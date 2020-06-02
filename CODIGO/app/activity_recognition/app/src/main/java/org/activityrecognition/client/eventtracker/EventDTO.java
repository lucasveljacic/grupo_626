package org.activityrecognition.client.eventtracker;

import java.io.Serializable;

public class EventDTO implements Serializable {
    private String env;
    private String type_events;
    private String state;
    private String description;

    public EventDTO(String env, String typeEvents, String state, String description) {
        this.env = env;
        this.type_events = typeEvents;
        this.state = state;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType_events() {
        return type_events;
    }

    public void setType_events(String type_events) {
        this.type_events = type_events;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "EventDTO{" +
                "env='" + env + '\'' +
                ", typeEvents='" + type_events + '\'' +
                ", state='" + state + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
