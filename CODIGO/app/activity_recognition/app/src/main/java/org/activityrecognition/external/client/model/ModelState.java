package org.activityrecognition.external.client.model;

public enum ModelState {
    NEW,
    COLLECTING_1,
    COLLECTED_1,
    COLLECTING_2,
    COLLECTED_2,
    TRAINING,
    READY_TO_SERVE,
    SERVING;
}
