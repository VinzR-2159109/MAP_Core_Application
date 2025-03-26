package be.uhasselt.dwi_application.model.Jackson.hands;

import be.uhasselt.dwi_application.model.basic.Position;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Hand {
    @JsonProperty("label")
    private HandLabel label;  // Enum: LEFT or RIGHT

    @JsonProperty("position")
    private Position position;

    @JsonProperty("status")
    private HandStatus status; // Enum: DETECTED or UNKNOWN

    @JsonProperty("landmarks")
    private List<LandmarkPosition> landmarks;

    public Hand() {}

    public Hand(HandLabel label, Position position, HandStatus status, List<LandmarkPosition> landmarks) {
        this.label = label;
        this.position = position;
        this.status = status;
        this.landmarks = landmarks;
    }

    public HandLabel getLabel() {
        return label;
    }
    public void setLabel(HandLabel label) {
        this.label = label;
    }

    public Position getPosition() {
        return position;
    }
    public void setPosition(Position position) {
        this.position = position;
    }

    public HandStatus getStatus() {
        return status;
    }
    public void setStatus(HandStatus status) {
        this.status = status;
    }

    public List<LandmarkPosition> getLandmarks() {return landmarks;}
    public void setLandmarks(List<LandmarkPosition> landmarks) {this.landmarks = landmarks;}

    @Override
    public String toString() {
        return "Hand{" +
                "label=" + label +
                ", position=" + position +
                ", status=" + status +
                ", landmarks=" + landmarks +
                '}';
    }
}
