package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.Pick.PickInstructionHandler;
import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.utility.database.repository.settings.SettingsRepository;
import be.uhasselt.dwi_application.utility.handTracking.HandTrackingHandler;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AssemblyInstructionHandler {
    private AssemblyInstruction assemblyInstruction;
    private AssemblyMQTTHelper helper;
    private Timer timer;
    private PickInstructionHandler.PickingHand pickingHand = PickInstructionHandler.PickingHand.RIGHT;
    private HandTrackingHandler handTracking = HandTrackingHandler.getInstance();
    private boolean isRunning = false;

    // Store last hand position on LED grid
    private int lastHandGridX = -1;
    private int lastHandGridY = -1;

    // Store assembly area boundaries
    private int startRangeX, endRangeX, startRangeY, endRangeY;

    public AssemblyInstructionHandler(){
        helper = new AssemblyMQTTHelper();
    }

    public void start(AssemblyInstruction assemblyInstruction, Runnable onCompleteCallback){
        helper.sendTurnOffAllLedStrip();
        handTracking.start();
        isRunning = true;

        this.assemblyInstruction = assemblyInstruction;

        List<Position> positions = assemblyInstruction.getAssemblyPositions();

        if (positions.isEmpty()) {
            System.out.println("No assembly positions available.");
            return;
        }

        int gridSize = SettingsRepository.loadSettings().getGridSize() / 2;

        // Get the min/max positions of the assembly area
        int startX = (int) positions.stream().mapToDouble(Position::getX).min().orElse(0);
        int endX = (int) positions.stream().mapToDouble(Position::getX).max().orElse(0);
        int startY = (int) positions.stream().mapToDouble(Position::getY).min().orElse(0);
        int endY = (int) positions.stream().mapToDouble(Position::getY).max().orElse(0);

        // Convert assembly area to LED grid indices
        startRangeX = 43 - endX / gridSize;
        endRangeX = 43 - startX / gridSize;
        startRangeY = 31 - endY / gridSize;
        endRangeY = 31 - startY / gridSize;

        // Send MQTT Commands for assembly area
        helper.sendSetLedStripXGreenOnRange(startRangeX, endRangeX);
        helper.sendSetLedStripYGreenOnRange(startRangeY, endRangeY);

        this.timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isRunning) return;

                double qualityScore = calculateQualityOfWorkScore();
                System.out.println("Quality Score: " + qualityScore);

                // Get hand position
                Position handPosition = handTracking.getRightHandPosition();
                if (handPosition != null) {
                    // Convert hand position to LED grid
                    int handGridX = 43 - (int) handPosition.getX() / gridSize;
                    int handGridY = 31 - (int) handPosition.getY() / gridSize;

                    // Only update if the hand has moved
                    if (handGridX != lastHandGridX || handGridY != lastHandGridY) {
                        // Restore last position:
                        if (lastHandGridX != -1 && lastHandGridY != -1) {
                            if (lastHandGridX >= startRangeX && lastHandGridX <= endRangeX) {
                                helper.sendSetLedStripXGreenOnRange(lastHandGridX, lastHandGridX + 2); // Restore to green
                            } else {
                                helper.sendSetLedStripXOff(lastHandGridX); // Turn off
                            }

                            if (lastHandGridY >= startRangeY && lastHandGridY <= endRangeY) {
                                helper.sendSetLedStripYGreenOnRange(lastHandGridY, lastHandGridY + 2); // Restore to green
                            } else {
                                helper.sendSetLedStripYOff(lastHandGridY); // Turn off
                            }
                        }

                        // Turn on new hand position LEDs
                        helper.sendSetLedStripXBlueOn(handGridX);
                        helper.sendSetLedStripYBlueOn(handGridY);
                        System.out.println("Hand position updated to LED: X=" + handGridX + " Y=" + handGridY);

                        // Update last known hand position
                        lastHandGridX = handGridX;
                        lastHandGridY = handGridY;
                    }
                } else {
                    System.out.println("Hand position not detected.");
                }

                // Check if assembly is completed
                if (qualityScore > 85) {
                    System.out.println("<Automatic Assembly Completion>");
                    stop();
                    onCompleteCallback.run();
                }
            }
        }, 0, 500);
    }

    public void stop(){
        helper.sendTurnOffAllLedStrip();

        if (timer != null){
            timer.cancel();
            timer.purge();
        }
        handTracking.stop();
        isRunning = false;
    }

    private double calculateQualityOfWorkScore() {
        Position handPosition = pickingHand == PickInstructionHandler.PickingHand.LEFT ? handTracking.getLeftHandPosition() : handTracking.getRightHandPosition();
        if (handPosition == null) {
            throw new IllegalStateException("Hand position is null!");
        }

        double avgX = assemblyInstruction.getAssemblyPositions().stream().mapToDouble(Position::getX).average().orElse(0);
        double avgY = assemblyInstruction.getAssemblyPositions().stream().mapToDouble(Position::getY).average().orElse(0);

        double distance = Math.sqrt(Math.pow(avgX - handPosition.getX(), 2) + Math.pow(avgY - handPosition.getY(), 2));

        double maxDistance = 550.0;
        double maxScore = 100;

        return Math.max(0, Math.min(maxScore, maxScore - (distance / maxDistance) * maxScore));
    }

    public boolean isRunning() {
        return isRunning;
    }
}
