package be.uhasselt.dwi_application.model.Jackson;

import com.fasterxml.jackson.annotation.*;

public class VibrationConfig {
    private final VibrationStatus status;
    private final int amplitude;
    @JsonProperty("vibration_ratio")
    private final double qow;

    public enum VibrationStatus {
        ON, OFF;

        @JsonValue
        public String toLower() {return name().toLowerCase();}
    }

    private VibrationConfig(VibrationStatus status, int amplitude, double qow) {
        this.status = status;
        this.amplitude = amplitude;
        this.qow = qow;
    }

    public VibrationStatus getStatus() {
        return status;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getAmplitude() {
        return status == VibrationStatus.OFF  ? null : amplitude;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Double getQow() {
        return status == VibrationStatus.OFF  ? null : qow;
    }

    public static VibrationConfig on(int amplitude, double qow) {
        return new VibrationConfig(VibrationStatus.ON, amplitude, qow);
    }

    public static VibrationConfig off() {
        return new VibrationConfig(VibrationStatus.OFF, 0, 0);
    }
}
