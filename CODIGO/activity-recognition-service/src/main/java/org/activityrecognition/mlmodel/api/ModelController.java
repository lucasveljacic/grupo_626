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

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/models")
public class ModelController {
    private final ModelService service;

    public ModelController(ModelService service) {
        this.service = service;
    }

    @RequestMapping(value = "/{id}",  method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> create(@PathVariable("id") String id, @RequestBody ModelDTO resource) {
        Preconditions.checkNotNull(resource);
        Model model = new Model();
        model.setId(resource.getId());
        model.setName(id);
        model.setState(ModelState.NEW);

        service.create(model);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}",  method = GET)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ModelDTO> create(@PathVariable("id") String id) {

        Model model = service.findById(id);
        if (model == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<ModelDTO>(entityToDTO(model), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/predict",  method = POST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PredictionOutputDTO> predict(@PathVariable("id") String id, @RequestBody PredictionInputDTO input) {

        Model model = service.findById(id);
        if (model == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        float prediction = service.predict(model, input.getInput());

        return new ResponseEntity<>(new PredictionOutputDTO(prediction), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/events/{event_id}",  method = POST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ModelDTO> event(@PathVariable("id") String id,
                                                     @PathVariable("event_id") ModelEvent eventId,
                                                     @RequestBody PredictionInputDTO input) {
        Model model = service.findById(id);
        if (model == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        service.handleEvent(model, eventId);
        return new ResponseEntity<>(entityToDTO(model), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/measures",  method = POST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PredictionOutputDTO> predict(@PathVariable("id") String id, @RequestBody MeasureRequest measureRequest) {
        Preconditions.checkNotNull(measureRequest);
        try {
            service.append(String.join("\n", measureRequest.getPacket()));
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ModelDTO entityToDTO(Model model) {
        ModelDTO dto = new ModelDTO();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setState(model.getState());
        return dto;
    }
}
