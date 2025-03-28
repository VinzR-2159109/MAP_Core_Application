package be.uhasselt.dwi_application.model.basic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Color {
    private int r;
    private int g;
    private int b;

    public enum BasicColors {
        BLUE, GREEN, RED;
    }

    @JsonCreator
    public Color(@JsonProperty("r") int r, @JsonProperty("g") int g, @JsonProperty("b") int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public static Color fromBasics(BasicColors basicColor) {
        if (basicColor == BasicColors.BLUE) {
            return new Color(0, 0, 255);
        }
        if (basicColor == BasicColors.GREEN) {
            return new Color(0, 255, 0);
        }
        if (basicColor == BasicColors.RED) {
            return new Color(255, 0, 0);
        }
        return null;
    }

    // Getters and Setters
    public int getR() { return r; }
    public void setR(int r) { this.r = r; }

    public int getG() { return g; }
    public void setG(int g) { this.g = g; }

    public int getB() { return b; }
    public void setB(int b) { this.b = b; }
}
