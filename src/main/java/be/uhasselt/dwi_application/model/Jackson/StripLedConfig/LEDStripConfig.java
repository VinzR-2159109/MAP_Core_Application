package be.uhasselt.dwi_application.model.Jackson.StripLedConfig;

import be.uhasselt.dwi_application.model.basic.Color;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

public class LEDStripConfig {
    private LEDStripId id;
    private int startRange;
    private int endRange;
    private Color color;
    private int brightness;
    private LEDStripStatus status;
    private int duration;
    private int cycles;

    public enum LEDStripStatus{
        ON, OFF, FLASH;

        @Override
        @JsonValue
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum LEDStripId{
        X,Y;

        @Override
        @JsonValue
        public String toString() {
            return name().toLowerCase();
        }
    }

    private LEDStripConfig(LEDStripId id, int startRange, int endRange, Color color, int brightness, LEDStripStatus status, int duration, int cycles) {
        this.id = id;
        this.startRange = startRange;
        this.endRange = endRange;

        this.color = color;
        this.brightness = brightness;
        this.status = status;
        this.duration = duration;
        this.cycles = cycles;
    }

    public static LEDStripConfig on(LEDStripId id, int startRange, int endRange, Color color, int brightness) {
        return new LEDStripConfig(id, startRange, endRange, color, brightness, LEDStripStatus.ON, -1, -1);
    }

    public static LEDStripConfig off(LEDStripId id, int startRange, int endRange) {
        return new LEDStripConfig(id, startRange, endRange, null, 0, LEDStripStatus.OFF, -1, -1);
    }

    public static LEDStripConfig pulse(LEDStripId id, int startRange, int endRange, Color color, int duration, int brightness) {
        return new LEDStripConfig(id, startRange, endRange, color, brightness, LEDStripStatus.FLASH, duration, 1);
    }

    public LEDStripId getId() {
        return id;
    }

    public String getRange() {
        return startRange + "-" + endRange;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Color getColor() {
        return status == LEDStripStatus.OFF ? null : color;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getBrightness() {
        return status == LEDStripStatus.OFF ? null : brightness;
    }

    public LEDStripStatus getStatus() {
        return status;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getDuration() {
        return status == LEDStripStatus.FLASH ? duration : null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getCycles() {
        return status == LEDStripStatus.FLASH ? cycles : null;
    }
}
