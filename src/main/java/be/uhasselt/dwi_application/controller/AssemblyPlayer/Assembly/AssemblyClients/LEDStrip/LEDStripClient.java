package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip;

import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripConfig;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripRange;
import be.uhasselt.dwi_application.model.basic.Color;
import be.uhasselt.dwi_application.model.basic.Range;
import be.uhasselt.dwi_application.utility.database.repository.settings.Settings;
import be.uhasselt.dwi_application.utility.modules.ConsoleColors;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class LEDStripClient {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int BASE_BRIGHTNESS = 50;
    private final String TOPIC = "Output/LEDStrip";
    private Settings settings;
    public enum Clients {
        MQTT, WS;
    }

    public LEDStripClient(Settings settings) {
        this.settings = settings;
    }

    public void sendON(LEDStripConfig.LEDStripId id, Range range, Color color) {
        LEDStripRange ledRange = LEDStripRange.on(range.start(), range.end(), color, BASE_BRIGHTNESS);
        LEDStripConfig config = new LEDStripConfig(id, List.of(ledRange));

        sendConfig(Clients.MQTT, config);
    }

    public void sendOFF(Clients client, LEDStripConfig.LEDStripId id, Range range) {
        LEDStripRange ledRange = LEDStripRange.off(range.start(), range.end());
        LEDStripConfig config = new LEDStripConfig(id, List.of(ledRange));

        sendConfig(client, config);
    }

    public void sendAllOFF() {
        System.out.println(ConsoleColors.RED + "<Turning off all LED strips>" + ConsoleColors.RESET);

        sendOFF(Clients.MQTT, LEDStripConfig.LEDStripId.X, new Range(0,43));
        sendOFF(Clients.MQTT, LEDStripConfig.LEDStripId.Y, new Range(0,28));
    }

    public void sendDirectionalLight(
            LEDStripConfig.LEDStripId id,
            Range range,
            double[] direction,
            double qowX,
            double qowY
    ) {
        int size = Math.max(0, range.end() - range.start() + 1);
        if (size <= 1) return;

        ArrayList<LEDStripRange> ranges = new ArrayList<>(size);

        double qowScore = (id == LEDStripConfig.LEDStripId.X) ? qowX : qowY;

        Color color;
        if (qowScore > settings.getNecessaryQOW()) {
            color = new Color(0, 255, 0);
        } else {
            double t = qowScore / 100.0;
            double adjustedGreen = Math.pow(t, 1.5);
            double adjustedRed = Math.pow(1 - t, 0.5);
            int r = (int) (255 * adjustedRed);
            int g = (int) (255 * adjustedGreen);
            color = new Color(r, g, 0);
        }

        int minBrightness = 10;
        int maxBrightness = 255;

        for (int i = 0; i < size; i++) {
            int index = range.start() + i;

            int relativeIndex;
            if (id == LEDStripConfig.LEDStripId.X) {
                relativeIndex = direction[id.ordinal()] < 0 ? i : (size - 1 - i);
            } else {
                relativeIndex = direction[id.ordinal()] > 0 ? i : (size - 1 - i);
            }

            double ratio = (double) relativeIndex / (size - 1);
            int brightness = minBrightness + (int) ((maxBrightness - minBrightness) * ratio * ratio);

            ranges.add(LEDStripRange.on(index, index, color, brightness));
        }

        LEDStripConfig config = new LEDStripConfig(id, ranges);
        sendConfig(Clients.WS, config);
    }


    private void sendConfig(Clients client, LEDStripConfig config) {
        String jsonConfig;
        try {
            jsonConfig = objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to serialize LED command", e);
        }

        switch (client) {
            case Clients.MQTT:
                MqttHandler.getInstance().publish(TOPIC, jsonConfig);

            case Clients.WS:
                LiveLightWebSocketEndpoint.broadcast(jsonConfig);
        }

    }

}
