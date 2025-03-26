package be.uhasselt.dwi_application.model.Jackson.hands;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LandmarkPosition {
    @JsonProperty("x")
    private int x;

    @JsonProperty("y")
    private int y;

    @JsonProperty("index")
    private String index;  // e.g. "WRIST", "THUMB_TIP", etc.

    public LandmarkPosition() {}

    public LandmarkPosition(int x, int y, String index) {
        this.x = x;
        this.y = y;
        this.index = index;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "LandmarkPosition{" +
                "x=" + x +
                ", y=" + y +
                ", index='" + index + '\'' +
                '}';
    }
}
