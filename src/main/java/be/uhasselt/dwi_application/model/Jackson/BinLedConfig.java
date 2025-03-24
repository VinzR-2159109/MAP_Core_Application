package be.uhasselt.dwi_application.model.Jackson;

import be.uhasselt.dwi_application.model.basic.Color;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BinLedConfig {
    private int id;
    private Color color;
    private int brightness;
    private String effect;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer duration;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer cycles;

    @JsonCreator
    public BinLedConfig(
            @JsonProperty("id") int id,
            @JsonProperty("color") Color color,
            @JsonProperty("brightness") int brightness,
            @JsonProperty("effect") String effect,
            @JsonProperty("duration") Integer duration,
            @JsonProperty("cycles") Integer cycles
    ) {
        this.id = id;
        this.color = color;
        this.brightness = brightness;
        this.effect = effect;

        if ("flash".equalsIgnoreCase(effect)) {
            this.duration = duration;
            this.cycles = cycles;
        }
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public int getBrightness() { return brightness; }
    public void setBrightness(int brightness) { this.brightness = brightness; }

    public String getEffect() { return effect; }
    public void setEffect(String effect) {
        this.effect = effect;

        if (!"flash".equalsIgnoreCase(effect)) {
            this.duration = null;
            this.cycles = null;
        }
    }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) {
        if ("flash".equalsIgnoreCase(this.effect)) {
            this.duration = duration;
        }
    }

    public Integer getCycles() { return cycles; }
    public void setCycles(Integer cycles) {
        if ("flash".equalsIgnoreCase(this.effect)) {
            this.cycles = cycles;
        }
    }
}
