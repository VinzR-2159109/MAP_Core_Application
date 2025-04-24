package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip;

import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripConfig;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripRange;
import be.uhasselt.dwi_application.model.basic.Color;
import be.uhasselt.dwi_application.utility.database.repository.settings.Settings;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import be.uhasselt.dwi_application.utility.network.NetworkClients;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class LEDConfigSender {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String topic = "Output/LEDStrip";
    private final Settings settings;

    public LEDConfigSender(Settings settings) {
        this.settings = settings;
    }

    public void sendOFF(NetworkClients client, LEDStripConfig.LEDStripId id, List<Integer> indices) {
        sendConfig(client, LEDStripRange.off(indices), id);
    }

    public void sendON(LEDStripConfig.LEDStripId id, List<Integer> indices, Color color, int brightness) {
        sendConfig(NetworkClients.MQTT, LEDStripRange.on(indices, color, brightness), id);
    }

    public void sendConfig(NetworkClients client, LEDStripRange range, LEDStripConfig.LEDStripId id) {
        try {
            LEDStripConfig config = new LEDStripConfig(id, List.of(range));
            String json = objectMapper.writeValueAsString(config);

            if (client == NetworkClients.MQTT) {
                MqttHandler.getInstance().publish(topic, json);
            } else {
                LiveLightWebSocketEndpoint.broadcast(json);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize LED command", e);
        }
    }

    public void sendConfig(NetworkClients client, LEDStripConfig config) {
        try {
            String json = objectMapper.writeValueAsString(config);
            if (client == NetworkClients.MQTT) {
                MqttHandler.getInstance().publish(topic, json);
            } else {
                LiveLightWebSocketEndpoint.broadcast(json);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize LED command", e);
        }
    }
}
