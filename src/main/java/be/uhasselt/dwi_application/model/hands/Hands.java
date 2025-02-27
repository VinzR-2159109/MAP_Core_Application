package be.uhasselt.dwi_application.model.hands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class Hands {
    @JsonProperty("hands")
    private List<Hand> detectedHands;

    public Hands() {}

    public Hands(List<Hand> detectedHands) {
        this.detectedHands = detectedHands;
    }

    public List<Hand> getDetectedHands() {
        return detectedHands;
    }

    public void setDetectedHands(List<Hand> detectedHands) {
        this.detectedHands = detectedHands;
    }

    public String toJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    @Override
    public String toString() {
        return toJson();
    }
}
