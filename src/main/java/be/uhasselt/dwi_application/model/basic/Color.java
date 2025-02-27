package be.uhasselt.dwi_application.model.basic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Color {
    private int r;
    private int g;
    private int b;

    @JsonCreator
    public Color(@JsonProperty("r") int r, @JsonProperty("g") int g, @JsonProperty("b") int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    // Getters and Setters
    public int getR() { return r; }
    public void setR(int r) { this.r = r; }

    public int getG() { return g; }
    public void setG(int g) { this.g = g; }

    public int getB() { return b; }
    public void setB(int b) { this.b = b; }
}
