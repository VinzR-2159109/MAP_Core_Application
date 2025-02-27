package be.uhasselt.dwi_application.model.hands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HandStatus {
    DETECTED("detected"),
    UNKNOWN("unknown");

    private final String status;

    HandStatus(String status) {
        this.status = status;
    }

    @JsonValue
    public String getStatus() {
        return status;
    }

    @JsonCreator
    public static HandStatus fromString(String value) {
        for (HandStatus handStatus : HandStatus.values()) {
            if (handStatus.status.equalsIgnoreCase(value)) {
                return handStatus;
            }
        }
        throw new IllegalArgumentException("Invalid hand status: " + value);
    }
}
