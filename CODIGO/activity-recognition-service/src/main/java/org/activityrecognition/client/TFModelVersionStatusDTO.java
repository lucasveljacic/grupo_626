package org.activityrecognition.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TFModelVersionStatusDTO {
    private String state;
    private String version;
    private StatusDTO status;

    @JsonCreator
    public TFModelVersionStatusDTO(@JsonProperty("state") String state,
                                   @JsonProperty("version") String version,
                                   @JsonProperty("status") StatusDTO status) {
        this.state = state;
        this.version = version;
        this.status = status;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public StatusDTO getStatus() {
        return status;
    }

    public void setStatus(StatusDTO status) {
        this.status = status;
    }

    private static class StatusDTO {
        private String errorCode;
        private String errorMessage;

        @JsonCreator
        public StatusDTO(@JsonProperty("error_code") String errorCode, @JsonProperty("error_message") String errorMessage) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
