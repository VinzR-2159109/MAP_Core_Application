package be.uhasselt.dwi_application.model.Jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DirectionConfig {
    @JsonProperty("x")
    private double x;

    @JsonProperty("y")
    private double y;

    @JsonProperty("status")
    private final DirectionStatus status;

    private enum DirectionStatus{
        UNKNOWN, KNOWN;

        @Override
        public String toString() {
            return super.toString();
        }
    }

    private DirectionConfig(double x, double y, DirectionStatus status) {
        this.x = x;
        this.y = y;
        this.status = status;
    }

    public static DirectionConfig config(double x, double y){
        return new DirectionConfig(x, y, DirectionStatus.KNOWN);
    }

    public static DirectionConfig unknown(){
        return new DirectionConfig(0, 0, DirectionStatus.UNKNOWN);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Double getX() {return status == DirectionStatus.UNKNOWN ? null : x;}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Double getY() {return status == DirectionStatus.UNKNOWN ? null : y;}

    public DirectionStatus getStatus() {return status;}
}
