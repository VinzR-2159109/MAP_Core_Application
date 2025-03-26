package be.uhasselt.dwi_application.model.Jackson.hands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HandLabel {
    LEFT("Left"),
    RIGHT("Right");

    private final String label;

    HandLabel(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static HandLabel fromString(String value) {
        for (HandLabel handLabel : HandLabel.values()) {
            if (handLabel.label.equalsIgnoreCase(value)) {
                return handLabel;
            }
        }
        throw new IllegalArgumentException("Invalid hand label: " + value);
    }
}
