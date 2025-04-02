package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyMQTTHelper;

import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripConfig;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripRange;
import be.uhasselt.dwi_application.model.basic.Color;
import be.uhasselt.dwi_application.model.basic.Range;
import be.uhasselt.dwi_application.utility.modules.ConsoleColors;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import be.uhasselt.dwi_application.utility.network.WebSocketClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class LEDStripMQTTHelper {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int BASE_BRIGHTNESS = 50;
    private final String TOPIC = "Output/LEDStrip";

    public void sendON(LEDStripConfig.LEDStripId id, Range range, Color color) {
        LEDStripRange ledRange = LEDStripRange.on(range.start(), range.end(), color, BASE_BRIGHTNESS);
        LEDStripConfig config = new LEDStripConfig(id, List.of(ledRange));

        sendConfig(config);
    }

    public void sendOFF(LEDStripConfig.LEDStripId id, Range range) {
        LEDStripRange ledRange = LEDStripRange.off(range.start(), range.end());
        LEDStripConfig config = new LEDStripConfig(id, List.of(ledRange));

        sendConfig(config);
    }

    public void sendAllOFF() {
        System.out.println(ConsoleColors.RED + "<Turning off all LED strips>" + ConsoleColors.RESET);

        sendOFF(LEDStripConfig.LEDStripId.X, new Range(0,43));
        sendOFF(LEDStripConfig.LEDStripId.Y, new Range(0,28));
    }

    private void sendConfig(LEDStripConfig config) {
        try {
            String jsonConfig = objectMapper.writeValueAsString(config);
            MqttHandler.getInstance().publish(TOPIC, jsonConfig);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to serialize LED command", e);
        }
    }

}
