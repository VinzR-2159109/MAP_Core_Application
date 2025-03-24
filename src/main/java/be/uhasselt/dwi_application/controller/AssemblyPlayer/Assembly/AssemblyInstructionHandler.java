package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.Pick.PickInstructionHandler;
import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.model.hands.HandStatus;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.utility.database.repository.settings.SettingsRepository;
import be.uhasselt.dwi_application.utility.handTracking.HandTrackingHandler;
import javafx.application.Platform;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class AssemblyInstructionHandler {
    private AtomicBoolean isCompleted;
    private Boolean isRunning;

    private AssemblyInstruction assemblyInstruction;
    private AssemblyMQTTHelper helper;
    private Timer timer;
    private PickInstructionHandler.PickingHand pickingHand = PickInstructionHandler.PickingHand.RIGHT;
    private final HandTrackingHandler handTracking = HandTrackingHandler.getInstance();

    public AssemblyInstructionHandler(){
        System.out.println("<Constructing AssemblyInstructionHandler>");

        this.helper = new AssemblyMQTTHelper();
        this.isCompleted = new AtomicBoolean(false);
        this.isRunning = false;
    }

    public void start(AssemblyInstruction assemblyInstruction, Runnable onCompleteCallback){
        System.out.println("\u001B[32m" +  "Starting Assembly Instruction Assistance>" + "\u001B[0m");

        isRunning = true;
        isCompleted.set(false);

        helper.sendTurnOffAllLedStrip();
        handTracking.start();

        this.assemblyInstruction = assemblyInstruction;

        List<Position> positions = assemblyInstruction.getAssemblyPositions();

        if (positions.isEmpty()) {
            System.out.println("No assembly positions available.");
            return;
        }

        int gridSize = SettingsRepository.loadSettings().getGridSize() / 2;

        helper.sendSetLedStripXGreenOnRange(
                43 - (int) positions.stream().mapToDouble(Position::getX).max().orElse(0) / gridSize,
                43 - (int) positions.stream().mapToDouble(Position::getX).min().orElse(0) / gridSize
        );

        helper.sendSetLedStripYGreenOnRange(
                31 - (int) positions.stream().mapToDouble(Position::getY).max().orElse(0) / gridSize,
                31 - (int) positions.stream().mapToDouble(Position::getY).min().orElse(0) / gridSize
        );


        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //System.out.println(calculateQualityOfWorkScore());
                if (calculateQualityOfWorkScore() > 85){
                    isCompleted.set(true);
                    stop();
                    Platform.runLater(onCompleteCallback);
                };

            }
        }, 0, 500);
    }

    public void stop(){
        System.out.println("<Stopping Assembly Instruction Assistance>");
        helper.sendTurnOffAllLedStrip();

        if (timer != null){
            timer.cancel();
            timer.purge();
        }

        handTracking.stop();
        isRunning = false;
    }

    private double calculateQualityOfWorkScore() {
        Position handPosition;
        if (pickingHand == PickInstructionHandler.PickingHand.LEFT && handTracking.getLeftHandStatus() != HandStatus.UNKNOWN){
            handPosition = handTracking.getLeftHandPosition();
        }
        else if (pickingHand == PickInstructionHandler.PickingHand.RIGHT && handTracking.getRightHandStatus() != HandStatus.UNKNOWN){
            handPosition = handTracking.getRightHandPosition();
        } else {
            return -1.0;
        }

        System.out.println("Right hand position: " + handPosition.getX() + " " + handPosition.getY());

        double avgX = assemblyInstruction.getAssemblyPositions().stream().mapToDouble(Position::getX).average().orElse(0);
        double avgY = assemblyInstruction.getAssemblyPositions().stream().mapToDouble(Position::getY).average().orElse(0);


        double distance = Math.sqrt(Math.pow(avgX - handPosition.getX(), 2) + Math.pow(avgY - handPosition.getY(), 2));
        System.out.println("Distance: " + distance);
        double maxDistance = 550;
        return Math.max(0, Math.min(100, 100 - (distance / maxDistance) * 100));
    }

    public boolean isCompleted() {return isCompleted.get();}

    public boolean isRunning() {return isRunning;}
}
