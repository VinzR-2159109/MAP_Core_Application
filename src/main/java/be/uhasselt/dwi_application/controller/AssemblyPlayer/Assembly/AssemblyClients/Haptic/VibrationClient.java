package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.Haptic;

import be.uhasselt.dwi_application.model.Jackson.VibrationConfig;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import be.uhasselt.dwi_application.utility.network.NetworkClients;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VibrationClient {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void vibrate(NetworkClients client, int amplitude, double qow) {
        VibrationConfig config = VibrationConfig.on(amplitude, qow);

        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to serialize Vibration command", e);
        }

        if (client == NetworkClients.MQTT){
            MqttHandler.getInstance().publish("Output/Vibration", jsonString);
        } else if (client == NetworkClients.WS){
            HapticWebSocketEndpoint.broadcast(jsonString);
        }
    }

    public void cancel(NetworkClients client){
        VibrationConfig config = VibrationConfig.off();

        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to serialize Vibration command", e);
        }

        if (client == NetworkClients.MQTT){
            MqttHandler.getInstance().publish("Output/Vibration", jsonString);
        } else if (client == NetworkClients.WS){
            HapticWebSocketEndpoint.broadcast(jsonString);
        }
    }
}
