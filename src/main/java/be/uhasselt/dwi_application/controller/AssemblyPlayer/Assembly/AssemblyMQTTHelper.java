package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly;

import be.uhasselt.dwi_application.model.Jackson.DirectionConfig;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripConfig;
import be.uhasselt.dwi_application.model.Jackson.VibrationConfig;
import be.uhasselt.dwi_application.model.basic.Color;
import be.uhasselt.dwi_application.model.basic.LEDStripRange;
import be.uhasselt.dwi_application.utility.modules.ConsoleColors;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssemblyMQTTHelper {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int BASE_BRIGHTNESS = 50;

    private void sendSetLedStripRange(LEDStripConfig.LEDStripId id, int start, int end, Color color, int brightness, LEDStripConfig.LEDStripStatus status) {
        if (status == LEDStripConfig.LEDStripStatus.ON) {
            LEDStripConfig stripON = LEDStripConfig.on(id, start, end, color, brightness);
            sendLedStripCommand(stripON);
        }
    }

    public void sendSetLedStrip(LEDStripConfig.LEDStripId id, LEDStripRange range, Color color) {
        LEDStripConfig Config = LEDStripConfig.on(id, range.start(), range.end(), color, BASE_BRIGHTNESS);
        sendLedStripCommand(Config);
    }

    public void sendSetLedStripXGreenOnRange(int start, int end) {
        sendSetLedStripRange(LEDStripConfig.LEDStripId.X, start, end, new Color(0, 255, 0), BASE_BRIGHTNESS, LEDStripConfig.LEDStripStatus.ON);
    }

    public void sendSetLedStripYGreenOnRange(int start, int end) {
        sendSetLedStripRange(LEDStripConfig.LEDStripId.Y, start, end, new Color(0, 255, 0), BASE_BRIGHTNESS, LEDStripConfig.LEDStripStatus.ON);
    }

    public void sendTurnOffAllLedStrip() {
        System.out.println(ConsoleColors.RED + "<Turning off all LED strips>" + ConsoleColors.RESET);

        LEDStripConfig stripXOFF = LEDStripConfig.off(LEDStripConfig.LEDStripId.X, 0, 43);
        LEDStripConfig stripYOFF = LEDStripConfig.off(LEDStripConfig.LEDStripId.Y, 0, 28);

        sendLedStripCommand(stripXOFF);
        sendLedStripCommand(stripYOFF);
    }

    public void sendLedStripCommand(LEDStripConfig config) {
        try {
            String jsonLEDStripConfig = objectMapper.writeValueAsString(config);
            MqttHandler.getInstance().publish("Output/LEDStrip", jsonLEDStripConfig);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to serialize LED command", e);
        }
    }

    public void sendVibrationCommand(int amplitude, double qow) {
        VibrationConfig config = VibrationConfig.on(amplitude, qow);

        try {
            String jsonString = objectMapper.writeValueAsString(config);
            MqttHandler.getInstance().publish("Output/Vibration", jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to serialize Vibration command", e);
        }
    }

    public void cancelVibration(){
        VibrationConfig config = VibrationConfig.off();

        try {
            String jsonVibration = objectMapper.writeValueAsString(config);
            MqttHandler.getInstance().publish("Output/Vibration", jsonVibration);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to serialize Vibration command", e);
        }
    }

    public void sendDirectionCommand(double x, double y) {
        DirectionConfig directionConfig = DirectionConfig.config(x,y);

        try {
            String jsonDirection = objectMapper.writeValueAsString(directionConfig);
            MqttHandler.getInstance().publish("Output/Direction", jsonDirection);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendDirectionUnknown() {
        DirectionConfig directionConfig = DirectionConfig.unknown();

        try {
            String jsonDirection = objectMapper.writeValueAsString(directionConfig);
            MqttHandler.getInstance().publish("Output/Direction", jsonDirection);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendFlashXBlueOnRange(int startX, int endX){
        LEDStripConfig pulseConfig = LEDStripConfig.pulse(LEDStripConfig.LEDStripId.X, startX, endX, Color.fromBasics(Color.BasicColors.BLUE), 50, BASE_BRIGHTNESS);

        try {
            String jsonPulseConfig = objectMapper.writeValueAsString(pulseConfig);
            MqttHandler.getInstance().publish(MqttHandler.Topics.LED_STRIP, jsonPulseConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendSetLedStripOnRange(LEDStripRange xRange, LEDStripRange yRange, Color.BasicColors basicColors) {
        LEDStripConfig blueXConfig = LEDStripConfig.on(LEDStripConfig.LEDStripId.X, xRange.start(), xRange.end(), Color.fromBasics(basicColors), BASE_BRIGHTNESS);
        LEDStripConfig blueYConfig = LEDStripConfig.on(LEDStripConfig.LEDStripId.Y, yRange.start(), yRange.end(), Color.fromBasics(basicColors), BASE_BRIGHTNESS);

        try {
            String jsonXConfig = objectMapper.writeValueAsString(blueXConfig);
            MqttHandler.getInstance().publish(MqttHandler.Topics.LED_STRIP, jsonXConfig);

            String jsonYConfig = objectMapper.writeValueAsString(blueYConfig);
            System.out.println(ConsoleColors.PURPLE_BOLD + "Sending LiveLight " + blueXConfig.getRange() + ConsoleColors.RESET);
            MqttHandler.getInstance().publish(MqttHandler.Topics.LED_STRIP, jsonYConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendTurnOffLedStripIdRange(LEDStripConfig.LEDStripId id, LEDStripRange range) {
        LEDStripConfig offConfig = LEDStripConfig.off(id, range.start(), range.end());

        try {
            String jsonXConfig = objectMapper.writeValueAsString(offConfig);
            MqttHandler.getInstance().publish(MqttHandler.Topics.LED_STRIP, jsonXConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendTurnOffLedStripRange(LEDStripRange lastXRange, LEDStripRange lastYRange) {
        LEDStripConfig offXConfig = LEDStripConfig.off(LEDStripConfig.LEDStripId.X, lastXRange.start(), lastXRange.end());
        LEDStripConfig offYConfig = LEDStripConfig.off(LEDStripConfig.LEDStripId.Y, lastYRange.start(), lastYRange.end());

        try {
            String jsonXConfig = objectMapper.writeValueAsString(offXConfig);
            MqttHandler.getInstance().publish(MqttHandler.Topics.LED_STRIP, jsonXConfig);

            String jsonYConfig = objectMapper.writeValueAsString(offYConfig);
            MqttHandler.getInstance().publish(MqttHandler.Topics.LED_STRIP, jsonYConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
