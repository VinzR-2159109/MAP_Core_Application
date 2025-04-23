package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip.Effect;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip.LEDConfigSender;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip.LEDStripClient;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripConfig;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripRange;
import be.uhasselt.dwi_application.model.basic.Color;
import be.uhasselt.dwi_application.model.basic.Range;
import be.uhasselt.dwi_application.utility.database.repository.settings.Settings;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GradientLightEffect {
    private final Settings settings;
    private final LEDConfigSender sender;

    public GradientLightEffect(Settings settings, LEDConfigSender sender) {
        this.settings = settings;
        this.sender = sender;
    }

    public void display(LEDStripConfig.LEDStripId id, Range assemblyRange) {
        int length = (id == LEDStripConfig.LEDStripId.X)
                ? settings.getXLEDLength()
                : settings.getYLEDLength();

        int start = assemblyRange.start();
        int end = assemblyRange.end();

        int maxDistance = Math.max(start, length - 1 - end); // normalize both sides

        List<LEDStripRange> gradient = IntStream.range(0, length)
                .mapToObj(i -> {
                    if (i >= start && i <= end) {
                        // Inside assembly range → pure green
                        return LEDStripRange.on(List.of(i), new Color(0, 255, 0), 255);
                    } else {
                        int distance = (i < start) ? start - i : i - end;
                        float t = Math.min((float) distance / maxDistance, 1f);
                        int g = (int) (165 * (1 - t)); // fade green down
                        return LEDStripRange.on(List.of(i), new Color(255, g, 0), 255); // red to orange
                    }
                })
                .collect(Collectors.toList());

        LEDStripConfig config = new LEDStripConfig(id, gradient);
        sender.sendConfig(LEDStripClient.Clients.WS, config);
    }


}

