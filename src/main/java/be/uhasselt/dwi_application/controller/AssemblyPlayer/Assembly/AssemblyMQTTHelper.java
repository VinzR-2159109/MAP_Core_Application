package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly;

import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripRange;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.StripLedConfig;
import be.uhasselt.dwi_application.model.Jackson.VibrationConfig;
import be.uhasselt.dwi_application.model.basic.Color;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class AssemblyMQTTHelper {

    private static final String MQTT_TOPIC = "Output/LEDStrip";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendSetLedStripRange(String stripId, int start, int end, Color color, int brightness, String status) {
        LEDStripRange range = new LEDStripRange(start, end, color, brightness, status);
        sendLedStripCommand(stripId, List.of(range));
    }

    public void sendSetLedStripXGreenOnRange(int start, int end) {
        sendSetLedStripRange("x", start, end, new Color(0, 255, 0), 50, "on");
    }

    public void sendSetLedStripYGreenOnRange(int start, int end) {
        sendSetLedStripRange("y", start, end, new Color(0, 255, 0), 50, "on");
    }

    public void sendTurnOffAllLedStrip() {
        System.out.println("<Turning Off All LED Strip>");
        List<LEDStripRange> ledsX = List.of(new LEDStripRange(0, 43, new Color(0, 0, 0), 0, "off"));
        List<LEDStripRange> ledsY = List.of(new LEDStripRange(0, 28, new Color(0, 0, 0), 0, "off"));

        sendLedStripCommand("x", ledsX);
        sendLedStripCommand("y", ledsY);
    }

    public void sendLedStripCommand(String stripId, List<LEDStripRange> ranges) {
        try {
            StripLedConfig ledConfig = new StripLedConfig(stripId, ranges);
            String jsonString = objectMapper.writeValueAsString(ledConfig);
            MqttHandler.getInstance().publish("Output/LEDStrip", jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to serialize LED command", e);
        }
    }

    public void sendVibrationCommand(int amplitude, double qow) {
        try {
            VibrationConfig config = VibrationConfig.on(amplitude, qow);
            String jsonString = objectMapper.writeValueAsString(config);
            MqttHandler.getInstance().publish("Output/Vibration", jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to serialize Vibration command", e);
        }
    }

    public void CancelVibration(){
        try {
            VibrationConfig config = VibrationConfig.off();
            String jsonString = objectMapper.writeValueAsString(config);
            MqttHandler.getInstance().publish("Output/Vibration", jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to serialize Vibration command", e);
        }
    }
}
