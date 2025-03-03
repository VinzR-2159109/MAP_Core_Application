package be.uhasselt.dwi_application.model.Jackson.StripLedConfig;

import be.uhasselt.dwi_application.model.basic.Color;

public class LEDStripRange {
    private int startRange;
    private int endRange;
    private Color color;
    private int brightness;
    private String status;

    public LEDStripRange(int startRange, int endRange, Color color, int brightness, String status) {
        this.startRange = startRange;
        this.endRange = endRange;

        this.color = color;
        this.brightness = brightness;
        this.status = status;
    }

    public String getRange() {
        return startRange + "-" + endRange;
    }

    public void setStartRange(int startRange) {this.startRange = startRange;}
    public void setEndRange(int endRange) {this.endRange = endRange;}

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
