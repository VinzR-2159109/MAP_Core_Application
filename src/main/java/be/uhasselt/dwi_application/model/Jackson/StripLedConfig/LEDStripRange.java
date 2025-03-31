package be.uhasselt.dwi_application.model.Jackson.StripLedConfig;

import be.uhasselt.dwi_application.model.basic.Color;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

public class LEDStripRange {
    private final int start;
    private final int end;
    private final Color color;
    private final int brightness;
    private final LEDStripStatus status;
    private final int duration;
    private final int cycles;

    public enum LEDStripStatus {
        ON, OFF, FLASH;

        @Override
        @JsonValue
        public String toString() {
            return name().toLowerCase();
        }
    }

    private LEDStripRange(int start, int end, Color color, int brightness, LEDStripStatus status, int duration, int cycles) {
        this.start = start;
        this.end = end;
        this.color = color;
        this.brightness = brightness;
        this.status = status;
        this.duration = duration;
        this.cycles = cycles;
    }

    public static LEDStripRange on(int start, int end, Color color, int brightness) {
        return new LEDStripRange(start, end, color, brightness, LEDStripStatus.ON, -1, -1);
    }

    public static LEDStripRange basic(int start, int end){
        return new LEDStripRange(start, end, null, -1, null, -1, -1);
    }

    public static LEDStripRange off(int start, int end) {
        return new LEDStripRange(start, end, null, 0, LEDStripStatus.OFF, -1, -1);
    }

    public static LEDStripRange flash(int start, int end, Color color, int brightness, int duration, int cycles) {
        return new LEDStripRange(start, end, color, brightness, LEDStripStatus.FLASH, duration, cycles);
    }

    public static LEDStripRange pulse(int start, int end, Color color, int duration, int brightness) {
        return flash(start, end, color, brightness, duration, 1);
    }


    public String getRange() {
        return start + "-" + end;
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
