package be.uhasselt.dwi_application.model.Jackson.StripLedConfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;

public class LEDStripConfig {
    private final LEDStripId id;

    @JsonProperty("list")
    private final List<LEDStripRange> rangeList;

    public enum LEDStripId{
        X,Y;

        @Override
        @JsonValue
        public String toString() {
            return name().toLowerCase();
        }
    }

    public LEDStripConfig(LEDStripId id, List<LEDStripRange> rangeList) {
        this.id = id;
        this.rangeList = rangeList;
    }

    public LEDStripId getId() {
        return id;
    }

    public List<LEDStripRange> getRangeList() {
        return rangeList;
    }
}
