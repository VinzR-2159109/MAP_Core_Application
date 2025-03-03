package be.uhasselt.dwi_application.model.Jackson.StripLedConfig;

import java.util.List;

public class StripLedConfig {
    private String id; //X or Y
    private List<LEDStripRange> leds;

    public StripLedConfig(String id, List<LEDStripRange> leds) {
        this.id = id;
        this.leds = leds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<LEDStripRange> getLeds() {
        return leds;
    }

    public void setLeds(List<LEDStripRange> leds) {
        this.leds = leds;
    }
}
