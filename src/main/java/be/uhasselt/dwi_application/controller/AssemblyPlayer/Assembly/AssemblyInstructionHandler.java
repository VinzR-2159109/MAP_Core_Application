package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.InstructionMeasurementHandler;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.Pick.PickInstructionHandler;
import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.model.Jackson.hands.HandStatus;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.utility.database.repository.settings.SettingsRepository;
import be.uhasselt.dwi_application.utility.handTracking.HandTrackingHandler;
import be.uhasselt.dwi_application.utility.modules.ConsoleColors;
import be.uhasselt.dwi_application.utility.modules.SoundPlayer;
import javafx.application.Platform;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class AssemblyInstructionHandler {
    private boolean isRunning;

    private Timer clk;
    private String sessionId;
    private AssemblyInstruction assemblyInstruction;
    private InstructionMeasurementHandler measurement;
    private PickInstructionHandler.PickingHand pickingHand = PickInstructionHandler.PickingHand.RIGHT;

    private final AssemblyMQTTHelper mqtt;
    private final AtomicBoolean isCompleted;
    private final HandTrackingHandler handTracking = HandTrackingHandler.getInstance();

    private HandStatus leftHandStatus = HandStatus.UNKNOWN;
    private HandStatus rightHandStatus = HandStatus.UNKNOWN;
    private HandStatus lastQoWStatus = HandStatus.UNKNOWN;

    public AssemblyInstructionHandler(String sessionId){
        System.out.println("<Constructing AssemblyInstructionHandler>");

        this.isRunning = false;

        this.mqtt = new AssemblyMQTTHelper();
        this.isCompleted = new AtomicBoolean(false);
        this.sessionId = sessionId;
    }

    public void start(AssemblyInstruction assemblyInstruction, Runnable onCompleteCallback) {
        System.out.println(ConsoleColors.GREEN + "Starting Assembly Instruction Assistance>" + ConsoleColors.RESET);

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

        this.measurement = new InstructionMeasurementHandler(assemblyInstruction.getAssembly(), assemblyInstruction, sessionId);
        measurement.startMeasurement();

        this.clk = new Timer();
        clk.schedule(new TimerTask() {
            @Override
            public void run() {
                updateDirection();
                updateVibrationFeedback(onCompleteCallback);
            }
        }, 0, 200);

    }

    public void stop(){
        if (!isRunning) return;

        System.out.println(ConsoleColors.RED + "<Stopping Assembly Instruction Assistance>" + ConsoleColors.RESET);

        measurement.stopMeasurement();

        mqtt.sendTurnOffAllLedStrip();
        mqtt.cancelVibration();

        if (clk != null){
            clk.cancel();
            clk.purge();
        }

        handTracking.stop();
        isRunning = false;
    }

    private void updateVibrationFeedback(Runnable onCompleteCallback) {
        boolean isRight = pickingHand == PickInstructionHandler.PickingHand.RIGHT;
        HandStatus currentStatus = isRight ? handTracking.getRightHandStatus() : handTracking.getLeftHandStatus();

        if (currentStatus == HandStatus.UNKNOWN) {
            if (lastQoWStatus != HandStatus.UNKNOWN) {
                mqtt.cancelVibration();
            }
            lastQoWStatus = currentStatus;
            return;
        }

        lastQoWStatus = currentStatus;

        Position handPosition = isRight ? handTracking.getRightHandPosition() : handTracking.getLeftHandPosition();
        int qowScore = calculateQualityOfWorkScore(handPosition);

        System.out.println("QoW Score: " + qowScore);
        mqtt.sendVibrationCommand((int) Math.round((qowScore / 100.0) * 255), qowScore);

        if (qowScore > 85) {
            isCompleted.set(true);
            SoundPlayer.play(SoundPlayer.SoundType.OK);
            mqtt.cancelVibration();
            stop();
            Platform.runLater(onCompleteCallback);
        }
    }


    private void updateDirection() {
        boolean isRight = pickingHand == PickInstructionHandler.PickingHand.RIGHT;

        HandStatus newStatus = isRight ? handTracking.getRightHandStatus() : handTracking.getLeftHandStatus();
        HandStatus cachedStatus = isRight ? rightHandStatus : leftHandStatus;

        if (!newStatus.equals(cachedStatus)) {
            if (isRight) rightHandStatus = newStatus;
            else leftHandStatus = newStatus;

            if (newStatus == HandStatus.UNKNOWN) {
                mqtt.sendDirectionUnknown();
                return;
            }
        }

        if (newStatus == HandStatus.UNKNOWN) {
            return;
        }

        Position handPosition = isRight ? handTracking.getRightHandPosition() : handTracking.getLeftHandPosition();
        double[] direction = calculateDirectionToAssembly(handPosition);
        mqtt.sendDirectionCommand(direction[0], direction[1]);
    }

    private int calculateQualityOfWorkScore(Position handPosition) {
        double avgX = assemblyInstruction.getAssemblyPositions()
                .stream()
                .mapToDouble(Position::getX)
                .average()
                .orElse(0.0);

        double avgY = assemblyInstruction.getAssemblyPositions()
                .stream()
                .mapToDouble(Position::getY)
                .average()
                .orElse(0.0);

        double distance = Math.sqrt(Math.pow(avgX - handPosition.getX(), 2) + Math.pow(avgY - handPosition.getY(), 2));

        double maxDistance = 550;
        return (int) Math.max(0, Math.min(100, 100 - (distance / maxDistance) * 100));
    }


    private double[] calculateDirectionToAssembly(Position handPosition) {
        double avgX = assemblyInstruction.getAssemblyPositions()
                .stream()
                .mapToDouble(Position::getX)
                .average()
                .orElse(0.0);

        double avgY = assemblyInstruction.getAssemblyPositions()
                .stream()
                .mapToDouble(Position::getY)
                .average()
                .orElse(0.0);

        double dx = avgX - handPosition.getX();
        double dy = avgY - handPosition.getY();

        double length = Math.sqrt(dx * dx + dy * dy);

        double relX = (length != 0) ? dx / length : 0;
        double relY = (length != 0) ? dy / length : 0;

        return new double[]{relX, relY};
    }



    public boolean isCompleted() {return isCompleted.get();}

    public boolean isRunning() {return isRunning;}
}
