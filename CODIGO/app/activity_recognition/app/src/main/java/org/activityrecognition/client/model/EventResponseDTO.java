package org.activityrecognition.client.model;

public class EventResponseDTO {
    private String message;
    private ModelDTO model;

    public EventResponseDTO(String message, ModelDTO modelDTO) {
        this.message = message;
        this.model = modelDTO;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ModelDTO getModel() {
        return model;
    }

    public void setModel(ModelDTO model) {
        this.model = model;
    }
}
