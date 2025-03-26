package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.Pick.PickInstructionHandler;
import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.model.Jackson.hands.HandStatus;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.utility.database.repository.settings.SettingsRepository;
import be.uhasselt.dwi_application.utility.handTracking.HandTrackingHandler;
import be.uhasselt.dwi_application.utility.modules.SoundPlayer;
import javafx.application.Platform;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class AssemblyInstructionHandler {
    private final AtomicBoolean isCompleted;
    private Boolean isRunning;

    private AssemblyInstruction assemblyInstruction;
    private final AssemblyMQTTHelper mqtt;
    private Timer timer;
    private PickInstructionHandler.PickingHand pickingHand = PickInstructionHandler.PickingHand.RIGHT;
    private final HandTrackingHandler handTracking = HandTrackingHandler.getInstance();

    public AssemblyInstructionHandler(){
        System.out.println("<Constructing AssemblyInstructionHandler>");

        this.mqtt = new AssemblyMQTTHelper();
        this.isCompleted = new AtomicBoolean(false);
        this.isRunning = false;
    }

    public void start(AssemblyInstruction assemblyInstruction, Runnable onCompleteCallback){
        System.out.println("\u001B[32m" +  "Starting Assembly Instruction Assistance>" + "\u001B[0m");

        isRunning = true;
        isCompleted.set(false);

        mqtt.sendTurnOffAllLedStrip();
        handTracking.start();

        this.assemblyInstruction = assemblyInstruction;

        List<Position> positions = assemblyInstruction.getAssemblyPositions();

        if (positions.isEmpty()) {
            System.out.println("No assembly positions available.");
            return;
        }

        int gridSize = SettingsRepository.loadSettings().getGridSize() / 2;

        mqtt.sendSetLedStripXGreenOnRange(
                43 - (int) positions.stream().mapToDouble(Position::getX).max().orElse(0) / gridSize,
                43 - (int) positions.stream().mapToDouble(Position::getX).min().orElse(0) / gridSize
        );

        mqtt.sendSetLedStripYGreenOnRange(
                31 - (int) positions.stream().mapToDouble(Position::getY).max().orElse(0) / gridSize,
                31 - (int) positions.stream().mapToDouble(Position::getY).min().orElse(0) / gridSize
        );


        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int qowScore = calculateQualityOfWorkScore();
                double[] direction = calculateDirectionToAssembly();
                System.out.println("Direction: " + direction[0] + " : " + direction[1]);
                if (qowScore < 0) return;

                System.out.println("QoW Score: " + qowScore);
                mqtt.sendVibrationCommand((int) Math.round((qowScore / 100.0) * 255), qowScore);

                if (qowScore > 85){
                    isCompleted.set(true);
                    SoundPlayer.play(SoundPlayer.SoundType.OK);

                    mqtt.CancelVibration();

                    stop();
                    Platform.runLater(onCompleteCallback);
                };

            }
        }, 0, 500);
    }

    public void stop(){
        System.out.println("<Stopping Assembly Instruction Assistance>");
        mqtt.sendTurnOffAllLedStrip();
        mqtt.CancelVibration();

        if (timer != null){
            timer.cancel();
            timer.purge();
        }

        handTracking.stop();
        isRunning = false;
    }

    private int calculateQualityOfWorkScore() {
        Position handPosition;
        if (pickingHand == PickInstructionHandler.PickingHand.LEFT && handTracking.getLeftHandStatus() != HandStatus.UNKNOWN){
            handPosition = handTracking.getLeftHandPosition();
        }
        else if (pickingHand == PickInstructionHandler.PickingHand.RIGHT && handTracking.getRightHandStatus() != HandStatus.UNKNOWN){
            handPosition = handTracking.getRightHandPosition();
        } else {
            return -1;
        }

        double avgX = assemblyInstruction.getAssemblyPositions().stream().mapToDouble(Position::getX).average().orElse(0);
        double avgY = assemblyInstruction.getAssemblyPositions().stream().mapToDouble(Position::getY).average().orElse(0);


        double distance = Math.sqrt(Math.pow(avgX - handPosition.getX(), 2) + Math.pow(avgY - handPosition.getY(), 2));

        double maxDistance = 550;
        return (int) Math.max(0, Math.min(100, 100 - (distance / maxDistance) * 100));
    }

    private double[] calculateDirectionToAssembly() {
        Position handPosition;
        double handRotation;

        if (pickingHand == PickInstructionHandler.PickingHand.LEFT && handTracking.getLeftHandStatus() != HandStatus.UNKNOWN) {
            handPosition = handTracking.getLeftHandPosition();
            handRotation = handTracking.getLeftHandRotation();
        } else if (pickingHand == PickInstructionHandler.PickingHand.RIGHT && handTracking.getRightHandStatus() != HandStatus.UNKNOWN) {
            handPosition = handTracking.getRightHandPosition();
            handRotation = handTracking.getRightHandRotation();
        } else {
            return new double[] {0.0, 0.0}; // Unknown or invalid
        }

        double avgX = assemblyInstruction.getAssemblyPositions().stream().mapToDouble(Position::getX).average().orElse(0);
        double avgY = assemblyInstruction.getAssemblyPositions().stream().mapToDouble(Position::getY).average().orElse(0);

        double dx = avgX - handPosition.getX();
        double dy = avgY - handPosition.getY();

        // Convert hand rotation from degrees to radians and invert to rotate coordinate system
//        double angleRad = Math.toRadians(-handRotation);
//
//        // Rotate the vector (dx, dy) by -handRotation
//        double relX = dx * Math.cos(angleRad) - dy * Math.sin(angleRad);
//        double relY = dx * Math.sin(angleRad) + dy * Math.cos(angleRad);
//
//        // Normalize to range [-1, 1]
//        double length = Math.sqrt(relX * relX + relY * relY);
//        if (length != 0) {
//            relX /= length;
//            relY /= length;
//        }

        double length = Math.sqrt(dx * dx + dy * dy);
        double relX = (length != 0) ? dx / length : 0;
        double relY = (length != 0) ? dy / length : 0;

        String direction = interpretDirection(relX, relY);
        System.out.printf("Direction to target: %s (%.2f, %.2f)%n", direction, relX, relY);

        return new double[] {relX, relY};
    }

    private String interpretDirection(double x, double y) {
        if (Math.abs(x) < 0.3 && y > 0.7) return "FORWARD";
        if (Math.abs(x) < 0.3 && y < -0.7) return "BACKWARD";
        if (x < -0.7 && Math.abs(y) < 0.3) return "LEFT";
        if (x > 0.7 && Math.abs(y) < 0.3) return "RIGHT";

        if (x < -0.5 && y > 0.5) return "FORWARD-LEFT";
        if (x > 0.5 && y > 0.5) return "FORWARD-RIGHT";
        if (x < -0.5 && y < -0.5) return "BACKWARD-LEFT";
        if (x > 0.5 && y < -0.5) return "BACKWARD-RIGHT";

        return "CENTERED";
    }


    public boolean isCompleted() {return isCompleted.get();}

    public boolean isRunning() {return isRunning;}
}
