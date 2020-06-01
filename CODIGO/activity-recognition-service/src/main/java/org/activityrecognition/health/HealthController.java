package org.activityrecognition.health;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class HealthController {

    @RequestMapping(value = "/health", method = GET, produces = "application/json")
    @ResponseBody
    public Map<String, String> health() {
        return ImmutableMap.of("status", "UP");
    }
}
