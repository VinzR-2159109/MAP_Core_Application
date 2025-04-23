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
import java.util.List;import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class LEDStripClient {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int BASE_BRIGHTNESS = 50;
    private final String TOPIC = "Output/LEDStrip";

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> xAnimationTask;
    private ScheduledFuture<?> yAnimationTask;

    private Settings settings;

    public enum Clients {
        MQTT, WS;
    }

    public LEDStripClient(Settings settings) {
        this.settings = settings;
    }

    public void sendON(LEDStripConfig.LEDStripId id, List<Integer> indices, Color color) {
        LEDStripRange ledRange = LEDStripRange.on(indices, color, BASE_BRIGHTNESS);
        LEDStripConfig config = new LEDStripConfig(id, List.of(ledRange));

        sendConfig(Clients.MQTT, config);
    }

    public void sendOFF(Clients client, LEDStripConfig.LEDStripId id, List<Integer> indices) {
        LEDStripRange ledRange = LEDStripRange.off(indices);
        LEDStripConfig config = new LEDStripConfig(id, List.of(ledRange));

        sendConfig(client, config);
    }

    public void sendOFF(Clients client, LEDStripConfig.LEDStripId id, Range range) {
        List<Integer> indices = IntStream.rangeClosed(range.start(), range.end()).boxed().toList();
        LEDStripRange ledRange = LEDStripRange.off(indices);
        LEDStripConfig config = new LEDStripConfig(id, List.of(ledRange));

        sendConfig(client, config);
    }

    public void sendAllOFF() {
        System.out.println(ConsoleColors.RED + "<Turning off all LED strips>" + ConsoleColors.RESET);

        List<Integer> xIndices = IntStream.range(0, settings.getXLEDLength()).boxed().collect(Collectors.toList());
        List<Integer> yIndices =  IntStream.range(0, settings.getYLEDLength()).boxed().collect(Collectors.toList());

        sendOFF(Clients.MQTT, LEDStripConfig.LEDStripId.X, xIndices);
        sendOFF(Clients.MQTT, LEDStripConfig.LEDStripId.Y, yIndices);
    }

    public void sendLiveLight(
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
        int cutoff = size / 2;

        boolean flip;
        if (id == LEDStripConfig.LEDStripId.X) {
            flip = direction[id.ordinal()] > 0; // light left
        } else {
            flip = direction[id.ordinal()] < 0; // light up
        }

        for (int i = 0; i < size; i++) {
            int index = range.start() + i;
            int brightness = (i < cutoff) == flip ? maxBrightness : minBrightness;
            ranges.add(LEDStripRange.on(List.of(index), color, brightness));
        }

        LEDStripConfig config = new LEDStripConfig(id, ranges);
        sendConfig(Clients.WS, config);
    }

    public void startFlowLight(LEDStripConfig.LEDStripId id, Range assemblyRange) {
        ScheduledFuture<?> animationTask = (id == LEDStripConfig.LEDStripId.X) ? xAnimationTask : yAnimationTask;
        if (animationTask != null && !animationTask.isDone()) {
            animationTask.cancel(true);
        }

        int stripStart = 0;
        int stripEnd = (id == LEDStripConfig.LEDStripId.X)
                ? settings.getXLEDLength() - 1
                : settings.getYLEDLength() - 1;

        int leftTarget, rightTarget;
        if (settings.getEnabledAssistanceSystemsAsList().contains(Settings.EnabledAssistanceSystem.STATIC_LIGHT)){
            leftTarget = assemblyRange.start() - 1;
            rightTarget = assemblyRange.end() + 1;
        } else {
            leftTarget = rightTarget = (assemblyRange.start() + assemblyRange.end()) / 2;
        }

        int[] leftIndex = {stripStart};
        int[] rightIndex = {stripEnd};

        Color flowColor = new Color(0, 0, 255);
        int brightness = 200;
        int trailSize = 3;

        Runnable task = () -> {
            List<LEDStripRange> updates = new ArrayList<>();

            // Left to target
            if (leftIndex[0] <= leftTarget) {
                int offIndex = leftIndex[0] - trailSize;
                if (offIndex >= stripStart) {
                    updates.add(LEDStripRange.off(List.of(offIndex)));
                }

                List<Integer> litTrail = IntStream.rangeClosed(
                        Math.max(stripStart, leftIndex[0] - trailSize + 1),
                        leftIndex[0]
                ).boxed().toList();
                updates.add(LEDStripRange.on(litTrail, flowColor, brightness));

                leftIndex[0]++;
            } else {
                // Clear remaining trail if needed
                IntStream.rangeClosed(leftIndex[0] - trailSize, leftIndex[0] - 1)
                        .filter(i -> i >= stripStart && i <= leftTarget)
                        .forEach(i -> updates.add(LEDStripRange.off(List.of(i))));
                leftIndex[0] = stripStart;
            }

            // Right to target
            if (rightIndex[0] >= rightTarget) {
                int offIndex = rightIndex[0] + trailSize;
                if (offIndex <= stripEnd) {
                    updates.add(LEDStripRange.off(List.of(offIndex)));
                }

                List<Integer> litTrail = IntStream.rangeClosed(
                        rightIndex[0],
                        Math.min(stripEnd, rightIndex[0] + trailSize - 1)
                ).boxed().toList();
                updates.add(LEDStripRange.on(litTrail, flowColor, brightness));

                rightIndex[0]--;
            } else {
                // Clear remaining trail if needed
                IntStream.rangeClosed(rightIndex[0] + 1, rightIndex[0] + trailSize)
                        .filter(i -> i <= stripEnd && i >= rightTarget)
                        .forEach(i -> updates.add(LEDStripRange.off(List.of(i))));
                rightIndex[0] = stripEnd;
            }

            LEDStripConfig config = new LEDStripConfig(id, updates);
            sendConfig(Clients.WS, config);
        };

        ScheduledFuture<?> newTask = scheduler.scheduleAtFixedRate(task, 0, 200, TimeUnit.MILLISECONDS);
        if (id == LEDStripConfig.LEDStripId.X) {
            xAnimationTask = newTask;
        } else {
            yAnimationTask = newTask;
        }
    }


    public void stopFlowLight() {
        if (xAnimationTask != null) {
            xAnimationTask.cancel(true);
        }
        if (yAnimationTask != null) {
            yAnimationTask.cancel(true);
        }
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
