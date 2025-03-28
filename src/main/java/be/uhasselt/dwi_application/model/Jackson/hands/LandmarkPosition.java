package be.uhasselt.dwi_application.model.Jackson.hands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class LandmarkPosition {
    @JsonProperty("x")
    private int x;

    @JsonProperty("y")
    private int y;

    @JsonProperty("index")
    private HandIndex index;  // e.g. "WRIST", "THUMB_TIP", etc.

    public enum HandIndex {
        WRIST,
        THUMB_CMC,
        THUMB_MCP,
        THUMB_IP,
        THUMB_TIP,
        INDEX_FINGER_MCP,
        INDEX_FINGER_PIP,
        INDEX_FINGER_DIP,
        INDEX_FINGER_TIP,
        MIDDLE_FINGER_MCP,
        MIDDLE_FINGER_PIP,
        MIDDLE_FINGER_DIP,
        MIDDLE_FINGER_TIP,
        RING_FINGER_MCP,
        RING_FINGER_PIP,
        RING_FINGER_DIP,
        RING_FINGER_TIP,
        PINKY_MCP,
        PINKY_PIP,
        PINKY_DIP,
        PINKY_TIP;

        public static HandIndex fromString(String index) {
            try {
                return HandIndex.valueOf(index.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        @JsonValue
        @Override
        public String toString() {
            return name().toUpperCase();
        }
    }


    public LandmarkPosition() {}

    public LandmarkPosition(int x, int y, String index) {
        this.x = x;
        this.y = y;
        this.index = HandIndex.fromString(index);
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

    public HandIndex getIndex() {
        return index;
    }

    public void setIndex(HandIndex index) {
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
