package org.activityrecognition.mlmodel.model;

import org.activityrecognition.client.TFPredictionRequestDTO;
import org.activityrecognition.client.TFPredictionResponseDTO;
import org.activityrecognition.client.TensorFlowServiceClient;
import org.activityrecognition.client.TensorFlowServiceClientFactory;
import org.activityrecognition.mlmodel.data.ModelRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
        return repository.save(model);
    }

    public Model findById(String id) {
        return repository.findById(id);
    }

    public float predict(Model model, float[][][] input) {

        TFPredictionRequestDTO request = new TFPredictionRequestDTO();


        TFPredictionResponseDTO response = tensorflowServiceClient.predict(model.getName(), request);
        float[][] prediction = response.getPredictions();

        return prediction[0][0];
    }

    public void handleEvent(Model model, ModelEvent eventId) {
        switch (eventId) {
            case FINISH_COLLECTING:
                trainModel(model);
        }
    }

    private void trainModel(Model model) {
        model.setState(ModelState.TRAINING);
        repository.save(model);

        //new Thread(() -> {

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

                // todo: check file SUCCESS had been written
                serveModel(model);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        //}).start();
    }

    private void serveModel(Model model) throws IOException {
        model.setState(ModelState.SERVING);
        repository.save(model);

        // notifying TensorFlow server the new model
        publishModels();
    }

    private void publishModels() throws IOException {
        List<Model> models = repository.findAll();

        String modelsConfig = models.stream()
                .filter(model -> model.getState() == ModelState.SERVING)
                .map(model -> {
                    return "  config {\n" +
                            "    name: \""+model.getName()+"\",\n" +
                            "    base_path: \""+String.format("%s/%s", MODELS_PATH, model.getName())+"\",\n" +
                            "    model_platform: \"tensorflow\",\n" +
                            "  }\n";
                }).collect(Collectors.joining());

        String content = "model_config_list {\n" + modelsConfig + "}\n";

        File file = new File(String.format("%s/%s", MODELS_PATH, "models.config"));
        FileWriter fr = new FileWriter(file, false);
        fr.write(content.trim());
        fr.close();
    }

    public void append(Model model, String packet, String suffix) throws IOException {
        logger.info(packet);

        File file = new File(String.format("%s/%s/measures_%s.csv", MODELS_PATH, model.getName(), suffix));
        FileWriter fr = new FileWriter(file, true);
        fr.write(packet.trim()+"\n");
        fr.close();
    }
}
