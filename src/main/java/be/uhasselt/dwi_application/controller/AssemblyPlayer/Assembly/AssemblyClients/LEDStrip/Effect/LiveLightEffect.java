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

public class LiveLightEffect {
    private final Settings settings;
    private final LEDConfigSender sender;

    public LiveLightEffect(Settings settings, LEDConfigSender sender) {
        this.settings = settings;
        this.sender = sender;
    }

    public void display(
            LEDStripConfig.LEDStripId id,
            Range range,
            double[] direction,
            double qowX,
            double qowY
    ) {
        int size = Math.max(0, range.end() - range.start() + 1);
        if (size <= 1) return;

        double qow = (id == LEDStripConfig.LEDStripId.X) ? qowX : qowY;
        Color color;

        if (qow > settings.getNecessaryQOW()) {
            color = new Color(0, 255, 0);
        } else {
            double t = qow / 100.0;
            int r = (int) (255 * Math.pow(1 - t, 0.5));
            int g = (int) (255 * Math.pow(t, 1.5));
            color = new Color(r, g, 0);
        }

        boolean flip = (id == LEDStripConfig.LEDStripId.X) ? direction[id.ordinal()] > 0
                : direction[id.ordinal()] < 0;

        List<LEDStripRange> output = new ArrayList<>();
        int cutoff = size / 2;

        for (int i = 0; i < size; i++) {
            int idx = range.start() + i;
            int brightness = (i < cutoff) == flip ? 255 : 10;
            output.add(LEDStripRange.on(List.of(idx), color, brightness));
        }

        sender.sendConfig(LEDStripClient.Clients.WS, new LEDStripConfig(id, output));
    }
}

