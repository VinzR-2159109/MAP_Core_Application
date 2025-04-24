package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip.Effect.FlowLightEffect;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip.Effect.GradientLightEffect;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip.Effect.LiveLightEffect;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripConfig;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripRange;
import be.uhasselt.dwi_application.model.basic.Color;
import be.uhasselt.dwi_application.model.basic.Range;
import be.uhasselt.dwi_application.utility.database.repository.settings.Settings;
import be.uhasselt.dwi_application.utility.database.repository.settings.SettingsRepository;
import be.uhasselt.dwi_application.utility.network.NetworkClients;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LEDStripClient {
    private final FlowLightEffect flowLight;
    private final GradientLightEffect gradientLight;
    private final LiveLightEffect liveLight;
    private final LEDConfigSender sender;

    private Settings settings = SettingsRepository.loadSettings();
    public enum Clients {
        MQTT, WS;
    }
    public LEDStripClient() {
        this.sender = new LEDConfigSender(settings);
        this.flowLight = new FlowLightEffect(settings, sender);
        this.gradientLight = new GradientLightEffect(settings, sender);
        this.liveLight = new LiveLightEffect(settings, sender);
    }


    public void sendON(NetworkClients client, LEDStripConfig.LEDStripId id, List<Integer> indices, Color color) {
        sendON(client, id, indices, color, 50);
    }

    public void sendON(NetworkClients client, LEDStripConfig.LEDStripId id, List<Integer> indices, Color color, int brightness) {
        LEDStripRange range = LEDStripRange.on(indices, color, brightness);
        LEDStripConfig config = new LEDStripConfig(id, List.of(range));
        sender.sendConfig(client, config);
    }


    public void startFlowLight(LEDStripConfig.LEDStripId id, Range range) {
        flowLight.start(id, range);
    }

    public void stopFlowLight() {
        flowLight.stop();
    }

    public void gradientLight(LEDStripConfig.LEDStripId id, Range assemblyRange) {
        gradientLight.display(id, assemblyRange);
    }

    public void sendLiveLight(LEDStripConfig.LEDStripId id, Range range, double[] dir, double qowX, double qowY) {
        liveLight.display(id, range, dir, qowX, qowY);
    }

    public void sendOFF(NetworkClients client, LEDStripConfig.LEDStripId id, List<Integer> indices) {
        sender.sendOFF(client, id, indices);
    }

    public void sendOFF(NetworkClients client, LEDStripConfig.LEDStripId id, Range range) {
        List<Integer> indices = IntStream.rangeClosed(range.start(), range.end()).boxed().collect(Collectors.toList());
        sender.sendOFF(client, id, indices);
    }

    public void sendAllOFF() {
        List<Integer> x = IntStream.range(0, settings.getXLEDLength()).boxed().toList();
        List<Integer> y = IntStream.range(0, settings.getYLEDLength()).boxed().toList();
        sender.sendOFF(NetworkClients.MQTT, LEDStripConfig.LEDStripId.X, x);
        sender.sendOFF(NetworkClients.MQTT, LEDStripConfig.LEDStripId.Y, y);
    }
}
