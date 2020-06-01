package org.activityrecognition.mlmodel.model;

import org.activityrecognition.client.TFPredictionRequestDTO;
import org.activityrecognition.client.TFPredictionResponseDTO;
import org.activityrecognition.client.TensorFlowServiceClient;
import org.activityrecognition.mlmodel.data.ModelRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

@Service
public class ModelService {
    private Logger logger = Logger.getLogger(ModelService.class.getName());
    private ModelRepository repository;
    private TensorFlowServiceClient tensorflowServiceClient;

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
        // todo: entrenar el modelo
    }

    public void append(String packet) throws IOException {
        logger.info(packet);

        File file = new File("/home/lucas/src/grupo_626/CODIGO/models/data/measures.csv");
        FileWriter fr = new FileWriter(file, true);
        fr.write(packet.trim()+"\n");
        fr.close();
    }
}
