package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.Haptic;

import be.uhasselt.dwi_application.model.Jackson.VibrationConfig;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VibrationMQTTHelper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void vibrate(int amplitude, double qow) {
        VibrationConfig config = VibrationConfig.on(amplitude, qow);

        try {
            String jsonString = objectMapper.writeValueAsString(config);
            MqttHandler.getInstance().publish("Output/Vibration", jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to serialize Vibration command", e);
        }
    }

    public void cancel(){
        VibrationConfig config = VibrationConfig.off();

        try {
            String jsonVibration = objectMapper.writeValueAsString(config);
            MqttHandler.getInstance().publish("Output/Vibration", jsonVibration);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to serialize Vibration command", e);
        }
    }
}
