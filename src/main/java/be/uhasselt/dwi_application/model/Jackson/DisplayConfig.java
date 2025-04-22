package be.uhasselt.dwi_application.model.Jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

public class DisplayConfig {
    private final Long id;
    private final int quantity;
    private final int brightness;
    private final DisplayStatus status;

    public enum DisplayStatus {
        ON, OFF;

        @JsonValue
        public String toLower() {
            return name().toLowerCase();
        }
    }

    public DisplayConfig(Long id, int number, int brightness, DisplayStatus status) {
        this.id = id;
        this.quantity = number;
        this.status = status;

        if (brightness > 7) throw new IllegalArgumentException("brightness exceeds 7");
        brightness = Math.max(brightness, 0);

        this.brightness = brightness;
    }

    public static DisplayConfig off(Long id) {
        return new DisplayConfig(id, 0, 0, DisplayStatus.OFF);
    }

    public static DisplayConfig on(Long id, int quantity) {
        return new DisplayConfig(id, quantity, 7, DisplayStatus.ON);
    }

    public Long getId() {return id;}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getQuantity() {return status == DisplayStatus.OFF ? null : quantity;}

    public DisplayStatus getStatus() { return status; }

    public int getBrightness() {return brightness;}


}
