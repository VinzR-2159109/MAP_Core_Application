package be.uhasselt.dwi_application.model.basic;

import javafx.util.Pair;

import java.util.ArrayList;

public class Range {
    private int start;
    private int end;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public static Range empty() {
        return new Range(-1, -1);
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public boolean rangesOverlap(Range other) {
        return end >= other.start && other.end >= start;
    }

    public boolean equalsRange(Range other) {
        return this.start == other.start && this.end == other.end;
    }

    public boolean resolvedOverlap(Range other) {
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

    public boolean isEmpty() {
        return start == -1 && end == -1;
    }

    @Override
    public String toString() {
        return "LEDStripRange[" + start + ", " + end + "]";
    }
}
