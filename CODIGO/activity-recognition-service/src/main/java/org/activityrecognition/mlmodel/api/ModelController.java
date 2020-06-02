package org.activityrecognition.mlmodel.api;

import com.google.common.base.Preconditions;
import org.activityrecognition.measure.MeasureRequest;
import org.activityrecognition.mlmodel.model.Model;
import org.activityrecognition.mlmodel.model.ModelEvent;
import org.activityrecognition.mlmodel.model.ModelService;
import org.activityrecognition.mlmodel.model.ModelState;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/models")
public class ModelController {
    private final Logger logger = Logger.getLogger(ModelController.class.getName());

    private final ModelService service;

    public ModelController(ModelService service) {
        this.service = service;
    }

    @RequestMapping(value = "/{id}",  method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ModelDTO> create(@PathVariable("id") String id) {
        Model model = new Model();
        model.setId(id);
        model.setName(id);
        model.setState(ModelState.NEW);

        model = service.create(model);
        return new ResponseEntity<>(entityToDTO(model), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}",  method = GET)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ModelDTO> get(@PathVariable("id") String id) {

        Model model = service.findById(id);
        if (model == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<ModelDTO>(entityToDTO(model), HttpStatus.OK);
    }

    @RequestMapping(method = GET)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ModelDTO>> getAll() {
        List<Model> models = service.findAll();
        return new ResponseEntity<>(entitiesToDTO(models), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/predictions",  method = POST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PredictionOutputDTO> predict(@PathVariable("id") String id, @RequestBody PredictionInputDTO input) {
        Model model = service.findById(id);
        if (model == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        logger.info(Arrays.deepToString(input.getInput()));

        float prediction = service.predict(model, input.getInput());

        return new ResponseEntity<>(new PredictionOutputDTO(prediction, "prediction succeeded"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/events/{event_id}",  method = POST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<EventResponseDTO> event(@PathVariable("id") String id,
                                                  @PathVariable("event_id") String eventId) {
        Model model = service.findById(id);
        if (model == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ModelEvent modelEvent;
        try {
            modelEvent = ModelEvent.valueOf(eventId.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    new EventResponseDTO(String.format("Event %s not supported", eventId), entityToDTO(model)),
                    HttpStatus.BAD_REQUEST);
        }
        service.handleEvent(model, modelEvent);
        return new ResponseEntity<>(new EventResponseDTO("Event handled Ok", entityToDTO(model)), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/measures/{user_id}",  method = POST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> collect(@PathVariable("id") String id,
                                        @PathVariable("user_id") String userId,
                                        @RequestBody MeasureRequest measureRequest) {
        Preconditions.checkNotNull(measureRequest);
        Model model = service.findById(id);
        if (model == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            service.append(model, String.join("\n", measureRequest.getPacket()), userId);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}",  method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> delete(@PathVariable("id") String id) throws IOException {
        Model model = service.findById(id);
        if (model != null) {
            service.delete(model);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private List<ModelDTO> entitiesToDTO(List<Model> models) {
        return models.stream().map(this::entityToDTO).collect(Collectors.toList());
    }

    private ModelDTO entityToDTO(Model model) {
        ModelDTO dto = new ModelDTO();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setState(model.getState());
        return dto;
    }
}
