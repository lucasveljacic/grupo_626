package org.activityrecognition.client.eventtracker;

import java.io.Serializable;

public class EventResponse implements Serializable {
    private String env;
    private String state;
    private EventDTO eventDTO;
    private String msg;

    public EventDTO getEventDTO() {
        return eventDTO;
    }

    public void setEventDTO(EventDTO eventDTO) {
        this.eventDTO = eventDTO;
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "EventResponse{" +
                "env='" + env + '\'' +
                ", state='" + state + '\'' +
                ", userDTO=" + eventDTO +
                ", msg='" + msg + '\'' +
                '}';
    }
}
