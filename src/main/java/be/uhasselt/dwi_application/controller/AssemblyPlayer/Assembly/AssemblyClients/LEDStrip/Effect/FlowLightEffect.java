package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip.Effect;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip.LEDConfigSender;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip.LEDStripClient;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripConfig;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripRange;
import be.uhasselt.dwi_application.model.basic.Color;
import be.uhasselt.dwi_application.model.basic.Range;
import be.uhasselt.dwi_application.utility.database.repository.settings.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class FlowLightEffect {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> xTask, yTask;
    private final Settings settings;
    private final LEDConfigSender sender;

    public FlowLightEffect(Settings settings, LEDConfigSender sender) {
        this.settings = settings;
        this.sender = sender;
    }

    public void start(LEDStripConfig.LEDStripId id, Range assemblyRange) {
        ScheduledFuture<?> animationTask = (id == LEDStripConfig.LEDStripId.X) ? xTask : yTask;
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
            sender.sendConfig(LEDStripClient.Clients.WS, config);
        };

        ScheduledFuture<?> newTask = scheduler.scheduleAtFixedRate(task, 0, 200, TimeUnit.MILLISECONDS);
        if (id == LEDStripConfig.LEDStripId.X) {
            xTask = newTask;
        } else {
            yTask = newTask;
        }
    }

    public void stop() {
        if (xTask != null) xTask.cancel(true);
        if (yTask != null) yTask.cancel(true);
    }
}
