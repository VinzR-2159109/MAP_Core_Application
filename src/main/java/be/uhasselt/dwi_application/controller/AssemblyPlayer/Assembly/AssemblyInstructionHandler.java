package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.InstructionMeasurementHandler;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripConfig;
import be.uhasselt.dwi_application.model.Jackson.hands.HandLabel;
import be.uhasselt.dwi_application.model.basic.Color;
import be.uhasselt.dwi_application.model.basic.LEDStripRange;
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

import static be.uhasselt.dwi_application.utility.modules.ConvertToStripCoords.convertToStripCoords;

public class AssemblyInstructionHandler {
    private boolean isRunning;
    private final int gridSize;

    private Timer clk;
    private final String sessionId;
    private AssemblyInstruction assemblyInstruction;
    private InstructionMeasurementHandler measurement;
    private final HandLabel pickingHand = HandLabel.RIGHT;

    private final AssemblyMQTTHelper mqtt;
    private final AtomicBoolean isCompleted;
    private final HandTrackingHandler handTracking = HandTrackingHandler.getInstance();

    private HandStatus leftHandStatus = HandStatus.UNKNOWN;
    private HandStatus rightHandStatus = HandStatus.UNKNOWN;
    private HandStatus lastQoWStatus = HandStatus.UNKNOWN;

    private LEDStripRange assemblyXRange;
    private LEDStripRange assemblyYRange;

    private LEDStripRange cachedBlueXRange;
    private LEDStripRange cachedBlueYRange;

    public AssemblyInstructionHandler(String sessionId){
        System.out.println("<Constructing AssemblyInstructionHandler>");

        this.gridSize = SettingsRepository.loadSettings().getGridSize() / 2;

        this.isRunning = false;

        this.mqtt = new AssemblyMQTTHelper();
        this.isCompleted = new AtomicBoolean(false);
        this.sessionId = sessionId;

        this.cachedBlueXRange = new LEDStripRange(-1, -1);
        this.cachedBlueYRange = new LEDStripRange(-1, -1);
    }

    public void start(AssemblyInstruction assemblyInstruction, Runnable onCompleteCallback) {
        if (isRunning) {
            System.out.println(ConsoleColors.RED_BOLD + "<AssemblyInstructionHandler Already Running>" + ConsoleColors.RESET);
            return;
        }

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

        int greenXStart = 43 - (int) positions.stream().mapToDouble(Position::getX).max().orElse(0) / gridSize;
        int greenXEnd   = 43 - (int) positions.stream().mapToDouble(Position::getX).min().orElse(0) / gridSize;
        int greenYStart = 31 - (int) positions.stream().mapToDouble(Position::getY).max().orElse(0) / gridSize;
        int greenYEnd   = 31 - (int) positions.stream().mapToDouble(Position::getY).min().orElse(0) / gridSize;

        assemblyXRange = new LEDStripRange(greenXStart, greenXEnd);
        assemblyYRange = new LEDStripRange(greenYStart, greenYEnd);

        System.out.println("AssemblyXRange: " + assemblyXRange);
        System.out.println("AssemblyYRange: " + assemblyYRange);

        mqtt.sendSetLedStrip(LEDStripConfig.LEDStripId.X, assemblyXRange, Color.fromBasics(Color.BasicColors.GREEN));
        mqtt.sendSetLedStrip(LEDStripConfig.LEDStripId.Y, assemblyYRange, Color.fromBasics(Color.BasicColors.GREEN));

        this.measurement = new InstructionMeasurementHandler(assemblyInstruction.getAssembly(), assemblyInstruction, sessionId);
        measurement.startMeasurement();

        this.clk = new Timer();
        clk.schedule(new TimerTask() {
            @Override
            public void run() {
                updateDirection();
                updateVibrationFeedback(onCompleteCallback);
                showLiveLight();
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
        HandStatus currentStatus = handTracking.getHandStatus(pickingHand);

        if (currentStatus == HandStatus.UNKNOWN) {
            if (lastQoWStatus != HandStatus.UNKNOWN) {
                mqtt.cancelVibration();
            }
            lastQoWStatus = currentStatus;
            return;
        }

        lastQoWStatus = currentStatus;

        Position handPosition = handTracking.getAvgHandPosition(pickingHand);
        int qowScore = calculateQualityOfWorkScore(handPosition);

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
        HandStatus newStatus = handTracking.getHandStatus(pickingHand);
        HandStatus cachedStatus = pickingHand == HandLabel.RIGHT ? rightHandStatus : leftHandStatus;

        if (!newStatus.equals(cachedStatus)) {
            if (pickingHand == HandLabel.RIGHT) rightHandStatus = newStatus;
            else leftHandStatus = newStatus;

            if (newStatus == HandStatus.UNKNOWN) {
                mqtt.sendDirectionUnknown();
                return;
            }
        }

        if (newStatus == HandStatus.UNKNOWN) {
            return;
        }

        Position avgHandPosition = handTracking.getAvgHandPosition(HandLabel.RIGHT);

        double[] direction = calculateDirectionToAssembly(avgHandPosition);
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

    private void showLiveLight() {
        if (handTracking.getHandStatus(pickingHand) == HandStatus.UNKNOWN) {
            mqtt.sendTurnOffLedStripIdRange(LEDStripConfig.LEDStripId.X, cachedBlueXRange);
            mqtt.sendTurnOffLedStripIdRange(LEDStripConfig.LEDStripId.Y, cachedBlueYRange);
            return;
        }

        Position avgHandPosition = handTracking.getAvgHandPosition(pickingHand);
        Position gridPosition = convertToStripCoords(avgHandPosition);

        int newXStart = (int) gridPosition.getX() - 2;
        int newXEnd   = (int) gridPosition.getX() + 2;
        int newYStart = (int) gridPosition.getY() - 2;
        int newYEnd   = (int) gridPosition.getY() + 2;

        LEDStripRange newXRange = new LEDStripRange(newXStart, newXEnd);
        LEDStripRange newYRange = new LEDStripRange(newYStart, newYEnd);

        if (!cachedBlueXRange.equalsRange(newXRange)) {
            mqtt.sendTurnOffLedStripIdRange(LEDStripConfig.LEDStripId.X, cachedBlueXRange);
            if (newXRange.resolvedOverlap(assemblyXRange)){
                mqtt.sendSetLedStrip(LEDStripConfig.LEDStripId.X, newXRange, Color.fromBasics(Color.BasicColors.BLUE));
            };
            cachedBlueXRange = newXRange;
        }

        if(!cachedBlueYRange.equalsRange(newYRange)) {
            mqtt.sendTurnOffLedStripIdRange(LEDStripConfig.LEDStripId.Y, cachedBlueYRange);
            if (newYRange.resolvedOverlap(assemblyYRange)){
                mqtt.sendSetLedStrip(LEDStripConfig.LEDStripId.Y, newYRange, Color.fromBasics(Color.BasicColors.BLUE));
            }
            cachedBlueYRange = newYRange;
        }
    }


    public boolean isCompleted() {return isCompleted.get();}

    public boolean isRunning() {return isRunning;}
}
