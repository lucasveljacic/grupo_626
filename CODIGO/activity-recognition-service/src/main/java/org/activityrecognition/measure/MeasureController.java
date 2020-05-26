package org.activityrecognition.measure;

import com.google.common.base.Preconditions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/measures")
public class MeasureController {
    private final MeasureService service;

    public MeasureController(MeasureService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MeasureResponse> create(@RequestBody MeasureRequest resource) {
        Preconditions.checkNotNull(resource);
        try {
            service.append(String.join("\n", resource.getPacket()));
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
