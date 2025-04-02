package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients;

import be.uhasselt.dwi_application.model.Jackson.DirectionConfig;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VisualDirectionMQTTHelper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendDirection(double[] direction) {
        DirectionConfig directionConfig = DirectionConfig.config(direction[0], direction[1]);

        try {
            String jsonDirection = objectMapper.writeValueAsString(directionConfig);
            MqttHandler.getInstance().publish("Output/Direction", jsonDirection);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUnknown() {
        DirectionConfig directionConfig = DirectionConfig.unknown();

        try {
            String jsonDirection = objectMapper.writeValueAsString(directionConfig);
            MqttHandler.getInstance().publish("Output/Direction", jsonDirection);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
