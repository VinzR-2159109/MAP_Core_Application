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

        int target = (assemblyRange.start() + assemblyRange.end()) / 2;

        List<LEDStripRange> gradient = IntStream.rangeClosed(0, Math.min(target, length - 1))
                .mapToObj(i -> {
                    float t = (float) i / target;
                    int r = (int) (255 * (1 - t));
                    int g = (int) (255 * t);
                    return LEDStripRange.on(List.of(i), new Color(r, g, 0), 255);
                })
                .collect(Collectors.toList());

        LEDStripConfig config = new LEDStripConfig(id, gradient);
        sender.sendConfig(LEDStripClient.Clients.WS, config);
    }
}

