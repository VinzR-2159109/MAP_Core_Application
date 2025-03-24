package be.uhasselt.dwi_application.controller.AssemblyPlayer.Pick;

import be.uhasselt.dwi_application.model.Jackson.BinLedConfig;
import be.uhasselt.dwi_application.model.Jackson.DisplayConfig;
import be.uhasselt.dwi_application.model.basic.Color;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PickMQTTHelper {
    public void SendSetBinLEDGreen(Long id) {
        BinLedConfig ledConfig = new BinLedConfig(
                id.intValue(),
                new Color(0, 255, 0),
                100,
                "on",
                null,
                null
        );

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(ledConfig);
            MqttHandler.getInstance().publish("Output/Bin/LED", jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void SendSetBinLEDOff(Long id) {
        BinLedConfig ledConfig = new BinLedConfig(
                id.intValue(),
                null,
                0,
                "off",
                null,
                null
        );

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(ledConfig);
            MqttHandler.getInstance().publish("Output/Bin/LED", jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void SendSetDisplayNumber(int id, int quantity) {
        DisplayConfig displayConfig = DisplayConfig.on(id, quantity);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(displayConfig);
            MqttHandler.getInstance().publish("Output/Bin/Display", jsonString);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void SendSetDisplayOff(int id) {
        DisplayConfig displayConfig = DisplayConfig.off(id);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(displayConfig);
            MqttHandler.getInstance().publish("Output/Bin/Display", jsonString);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
