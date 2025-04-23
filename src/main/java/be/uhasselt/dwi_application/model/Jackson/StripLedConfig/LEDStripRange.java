package be.uhasselt.dwi_application.model.Jackson.StripLedConfig;

import be.uhasselt.dwi_application.model.basic.Color;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public class LEDStripRange {
    @JsonProperty("list_indices")
    private final List<Integer> listIndices;
    private final Color color;
    private final int brightness;
    private final LEDStripStatus status;

    public enum LEDStripStatus {
        ON, OFF;

        @Override
        @JsonValue
        public String toString() {
            return name().toLowerCase();
        }
    }

    private LEDStripRange(List<Integer> listIndices, Color color, int brightness, LEDStripStatus status) {
        this.listIndices = listIndices;
        this.color = color;
        this.brightness = brightness;
        this.status = status;
    }

    public static LEDStripRange on(List<Integer> indices, Color color, int brightness) {
        return new LEDStripRange(indices, color, brightness, LEDStripStatus.ON);
    }

    public static LEDStripRange off(List<Integer> indices) {
        return new LEDStripRange(indices, null, 0, LEDStripStatus.OFF);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Integer> getListIndices() {
        return listIndices;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Color getColor() {
        return status == LEDStripStatus.OFF ? null : color;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getBrightness() {return status == LEDStripStatus.OFF ? null : brightness;}

    public LEDStripStatus getStatus() {return status;}
}
