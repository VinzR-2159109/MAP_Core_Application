package be.uhasselt.dwi_application.model.basic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Position implements Serializable {
    @JsonProperty("x")
    private double x;

    @JsonProperty("y")
    private double y;

    public Position() {}

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "{x=" + x + ", y=" + y + "}";
    }
}
