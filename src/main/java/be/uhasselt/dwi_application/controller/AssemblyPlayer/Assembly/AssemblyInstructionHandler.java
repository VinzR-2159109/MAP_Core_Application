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

        // Determine startX, endX for LED Strip X
        int startX = (int) positions.stream().mapToDouble(Position::getX).min().orElse(0);
        int endX = (int) positions.stream().mapToDouble(Position::getX).max().orElse(0);

        // Determine startY, endY for LED Strip Y
        int startY = (int) positions.stream().mapToDouble(Position::getY).min().orElse(0);
        int endY = (int) positions.stream().mapToDouble(Position::getY).max().orElse(0);


        int gridSize = SettingsRepository.loadSettings().getGridSize() / 2;

        int startRangeX = 43 - endX/gridSize;
        int endRangeX = 43 - startX/gridSize;

        int startRangeY = 31 - endY/gridSize;
        int endRangeY = 31 -  startY/gridSize;

        // Send MQTT Commands
        helper.sendSetLedStripXGreenOnRange(startRangeX, endRangeX);
        helper.sendSetLedStripYGreenOnRange(startRangeY, endRangeY);

        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(calculateQualityOfWorkScore());
                if (calculateQualityOfWorkScore() > 85){
                    System.out.println("<Automatic Assembly Completion>");
                    onCompleteCallback.run();
                };

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
