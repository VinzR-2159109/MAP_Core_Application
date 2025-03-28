package be.uhasselt.dwi_application.model.basic;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.io.Serializable;

public class Position {
    private Long id;

    @JsonProperty("x")
    private double x;

    @JsonProperty("y")
    private double y;

    public Position() {}

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @JdbiConstructor
    public Position(@ColumnName("id") Long id, @ColumnName("x") double x, @ColumnName("y") double y){
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public Long getId(){return id;}
    public void setId(Long id) { this.id = id; }

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

    public boolean isFarEnoughFrom(Position other, double minDistance) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double distanceSquared = dx * dx + dy * dy;
        return distanceSquared >= (minDistance * minDistance);
    }


    @Override
    public String toString() {
        return "{x=" + x + ", y=" + y + "}";
    }
}
