package be.uhasselt.dwi_application.model.basic;

public class LEDStripRange {
    private int start;
    private int end;

    public LEDStripRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public boolean rangesOverlap(LEDStripRange other) {
        return end >= other.start && other.end >= start;
    }

    public boolean equalsRange(LEDStripRange other) {
        return this.start == other.start && this.end == other.end;
    }

    public boolean resolvedOverlap(LEDStripRange other) {
        if (!rangesOverlap(other)) return true;

        if (other.start <= start && other.end >= end) {
            start = -1;
            end = -1;
            return false;
        }

        if (other.start <= start && other.end < end) {
            start = other.end + 1;
        } else if (other.start > start && other.start <= end) {
            end = other.start - 1;
        }

        return start <= end;
    }


    @Override
    public String toString() {
        return "LEDStripRange[" + start + ", " + end + "]";
    }
}
