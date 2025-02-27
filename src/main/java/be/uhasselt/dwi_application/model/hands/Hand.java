package be.uhasselt.dwi_application.model.hands;

import be.uhasselt.dwi_application.model.basic.Position;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Hand {
    @JsonProperty("hand")
    private HandLabel label;  // Enum: LEFT or RIGHT

    @JsonProperty("position")
    private Position position;

    @JsonProperty("status")
    private HandStatus status; // Enum: DETECTED or UNKNOWN

    public Hand() {}

    public Hand(HandLabel label, Position position, HandStatus status) {
        this.label = label;
        this.position = position;
        this.status = status;
    }

    public HandLabel getLabel() {
        return label;
    }

    public Position getPosition() {
        return position;
    }

    public HandStatus getStatus() {
        return status;
    }

    public void setLabel(HandLabel label) {
        this.label = label;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setStatus(HandStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Hand{label=" + label + ", position=" + position + ", status=" + status + "}";
    }
}
