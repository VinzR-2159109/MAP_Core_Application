package be.uhasselt.dwi_application.model.basic;

public class Time {
    private final double timeInSeconds;

    public Time(double timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    public double getSeconds() {
        return timeInSeconds;
    }

    public double getMinutes() {
        return timeInSeconds / 60.0;
    }

    public String getReadable() {
        int minutes = (int) (timeInSeconds / 60);
        int seconds = (int) (timeInSeconds % 60);
        return String.format("%d min %02d sec", minutes, seconds);
    }
}
