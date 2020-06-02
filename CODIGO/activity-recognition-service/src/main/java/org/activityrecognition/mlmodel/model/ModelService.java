package org.activityrecognition.mlmodel.model;

import org.activityrecognition.client.*;
import org.activityrecognition.mlmodel.data.ModelRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ModelService {
    private static final String MODELS_PATH = "/var/models";
    private static final String SCRIPTS_PATH = "/opt/activity-recognition";
    private final Logger logger = Logger.getLogger(ModelService.class.getName());
    private final ModelRepository repository;
    private final TensorFlowServiceClient tensorflowServiceClient = TensorFlowServiceClientFactory.getClient();

    public ModelService(ModelRepository repository) {
        this.repository = repository;
    }
    public Model create(Model model) {
        // checking if model is in serving state already
        if (isBeenServed(model)) {
            model.setState(ModelState.SERVING);
        }

        // create directory structure
        File trainingPath = new File(String.format("%s/%s/train", MODELS_PATH, model.getName()));
        if (!trainingPath.exists()) {
            trainingPath.mkdirs();
        }

        File servingPath = new File(String.format("%s/%s/model", MODELS_PATH, model.getName()));
        if (!servingPath.exists()) {
            servingPath.mkdirs();
        }

        return repository.save(model);
    }

    public Model findById(String id) {
        return repository.findById(id);
    }

    public float predict(Model model, float[][][] input) {

        TFPredictionRequestDTO request = new TFPredictionRequestDTO(input);

        TFPredictionResponseDTO response = tensorflowServiceClient.predict(model.getName(), request);
        float[][] prediction = response.getPredictions();

        return prediction[0][0];
    }

    public void handleEvent(Model model, ModelEvent eventId) throws IOException {
        switch (eventId) {
            case END_COLLECT_1:
                model.setState(ModelState.COLLECTED_1);
                repository.save(model);
                break;
            case END_COLLECT_2:
                model.setState(ModelState.COLLECTED_2);
                repository.save(model);
                break;
            case START_TRAINING:
                trainModel(model);
                break;
            case RESET:
                delete(model);
                create(model);
                break;
        }
    }

    private void trainModel(Model model) {
        model.setState(ModelState.TRAINING);
        repository.save(model);

        new Thread(() -> {
            ProcessBuilder pb = new ProcessBuilder(
                    "bash",
                    "run.sh",
                    String.format("--basepath %s/%s", MODELS_PATH, model.getName()));

            pb.directory(new File(SCRIPTS_PATH));
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            try {
                Process p = pb.start();
                p.waitFor(5, TimeUnit.MINUTES);
                serveModel(model);

            } catch (IOException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void serveModel(Model model) throws IOException, TimeoutException, InterruptedException {
        // notifying TensorFlow server the new model
        model.setState(ModelState.READY_TO_SERVE);
        repository.save(model);

        publishModels();

        int totalSleepSeconds = 0;
        while (!isBeenServed(model)) {
            if (totalSleepSeconds >= 60) {
                throw new TimeoutException("Task execution timed out!");
            }
            Thread.sleep(5 * 1000);
            totalSleepSeconds += 5;
        }

        model.setState(ModelState.SERVING);
        repository.save(model);
    }

    private void publishModels() throws IOException {
        List<Model> models = repository.findAll();

        String modelsConfig = models.stream()
                .filter(model -> model.getState() == ModelState.SERVING || model.getState() == ModelState.READY_TO_SERVE)
                .map(model -> {
                    return "  config {\n" +
                            "    name: \""+model.getName()+"\",\n" +
                            "    base_path: \""+String.format("%s/%s/model", MODELS_PATH, model.getName())+"\",\n" +
                            "    model_platform: \"tensorflow\",\n" +
                            "  }\n";
                }).collect(Collectors.joining());

        if (!modelsConfig.isEmpty()) {
            modelsConfig = "model_config_list {\n" + modelsConfig + "}\n";
        }

        File file = new File(String.format("%s/%s", MODELS_PATH, "models.config"));
        FileWriter fr = new FileWriter(file, false);
        fr.write(modelsConfig.trim());
        fr.close();
    }

    public void append(Model model, String packet, String suffix) throws IOException {
        logger.info(packet);
        boolean append = true;

        if (model.getState() == ModelState.NEW && suffix.equals("1")) {
            model.setState(ModelState.COLLECTING_1);
            append = false;
        }
        if (model.getState() == ModelState.COLLECTED_1 && suffix.equals("2")) {
            model.setState(ModelState.COLLECTING_2);
            append = false;
        }

        File file = getMeasuresFile(model, suffix);
        FileWriter fr = new FileWriter(file, append);
        fr.write(packet.trim()+"\n");
        fr.close();
    }

    public List<Model> findAll() {
        return repository.findAll();
    }

    public void delete(Model model) throws IOException {
        deleteDirectory(new File(String.format("%s/%s", MODELS_PATH, model.getName())).toPath());
        model.setState(ModelState.NEW);

        repository.save(model);
        publishModels();
    }

    private File getMeasuresFile(Model model, String suffix) {
        return new File(String.format("%s/%s/train/measures_%s.csv", MODELS_PATH, model.getName(), suffix));
    }

    private void deleteDirectory(Path path) {
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isBeenServed(Model model) {
        TFModelDTO tfModelDTO;
        try {
            tfModelDTO = tensorflowServiceClient.findModel(model.getName());
            if (tfModelDTO.getModelVersionStatus() != null && !tfModelDTO.getModelVersionStatus().isEmpty()) {
                if (tfModelDTO.getModelVersionStatus().get(0).getState().equals("AVAILABLE")) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
}
