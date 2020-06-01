package org.activityrecognition.mlmodel.api;

import org.activityrecognition.mlmodel.model.ModelState;

public class ModelDTO {
    private String id;
    private ModelState state;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ModelState getState() {
        return state;
    }

    public void setState(ModelState state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
