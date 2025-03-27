package be.uhasselt.dwi_application.utility.modules;

import be.uhasselt.dwi_application.model.basic.Time;

public class ElapsedTimer {
    private long startTime;
    private long endTime;
    private boolean running;

    public void start() {
        startTime = System.nanoTime();
        running = true;
    }

    public void stop() {
        endTime = System.nanoTime();
        running = false;
    }

    public Time getElapsedTime() {
        long elapsed;
        if (running) {
            elapsed = System.nanoTime() - startTime;
        } else {
            elapsed = endTime - startTime;
        }
        return new Time(elapsed / 1_000_000_000.0);
    }
}
