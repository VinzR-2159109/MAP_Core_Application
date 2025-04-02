package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.VisualDirectionMQTTHelper;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStripClient;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.VibrationMQTTHelper;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.InstructionMeasurementHandler;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripConfig;
import be.uhasselt.dwi_application.model.Jackson.hands.HandLabel;
import be.uhasselt.dwi_application.model.basic.Color;
import be.uhasselt.dwi_application.model.basic.Range;
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

    private final LEDStripClient ledStrip;
    private final VibrationMQTTHelper vibration;
    private final VisualDirectionMQTTHelper directionMqtt;

    private final AtomicBoolean isCompleted;
    private final HandTrackingHandler handTracking = HandTrackingHandler.getInstance();

    private HandStatus leftHandStatus = HandStatus.UNKNOWN;
    private HandStatus rightHandStatus = HandStatus.UNKNOWN;
    private HandStatus lastQoWStatus = HandStatus.UNKNOWN;

    private Range assemblyXRange;
    private Range assemblyYRange;

    private Range cachedBlueXRange;
    private Range cachedBlueYRange;

    public AssemblyInstructionHandler(String sessionId){
        System.out.println("<Constructing AssemblyInstructionHandler>");

        this.gridSize = SettingsRepository.loadSettings().getGridSize() / 2;

        this.isRunning = false;

        this.ledStrip = new LEDStripClient();
        this.vibration = new VibrationMQTTHelper();
        this.directionMqtt = new VisualDirectionMQTTHelper();

        this.isCompleted = new AtomicBoolean(false);
        this.sessionId = sessionId;

        this.cachedBlueXRange = new Range(-1, -1);
        this.cachedBlueYRange = new Range(-1, -1);
    }

    public void start(AssemblyInstruction assemblyInstruction, Runnable onCompleteCallback) {
        if (isRunning) {
            System.out.println(ConsoleColors.RED_BOLD + "<AssemblyInstructionHandler Already Running>" + ConsoleColors.RESET);
            return;
        }

        System.out.println(ConsoleColors.GREEN + "Starting Assembly Instruction Assistance>" + ConsoleColors.RESET);

        isRunning = true;
        isCompleted.set(false);

        ledStrip.sendAllOFF();
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

        assemblyXRange = new Range(greenXStart, greenXEnd);
        assemblyYRange = new Range(greenYStart, greenYEnd);

        System.out.println("AssemblyXRange: " + assemblyXRange);
        System.out.println("AssemblyYRange: " + assemblyYRange);

        ledStrip.sendON(LEDStripConfig.LEDStripId.X, assemblyXRange, Color.fromBasics(Color.BasicColors.GREEN));
        ledStrip.sendON(LEDStripConfig.LEDStripId.Y, assemblyYRange, Color.fromBasics(Color.BasicColors.GREEN));

        this.measurement = new InstructionMeasurementHandler(assemblyInstruction.getAssembly(), assemblyInstruction, sessionId);
        measurement.startMeasurement();

        this.clk = new Timer();
        clk.schedule(new TimerTask() {
            @Override
            public void run() {
                Position avgHandPosition = handTracking.getAvgHandPosition(HandLabel.RIGHT);
                double[] direction = calculateDirectionToAssembly(avgHandPosition);
                int qow = calculateQualityOfWorkScore(avgHandPosition);

                updateDirection(direction);
                updateVibrationFeedback();

                if (qow > 85) {
                    isCompleted.set(true);
                    SoundPlayer.play(SoundPlayer.SoundType.OK);
                    vibration.cancel();
                    stop();
                    Platform.runLater(onCompleteCallback);
                }
                showLiveLight(direction, qow);
            }
        }, 0, 200);

    }

    public void stop(){
        if (!isRunning) return;
        isRunning = false;

        System.out.println(ConsoleColors.RED + "<Stopping Assembly Instruction Assistance>" + ConsoleColors.RESET);

        measurement.stopMeasurement();

        ledStrip.sendAllOFF();
        vibration.cancel();

        if (clk != null){
            clk.cancel();
            clk.purge();
        }

        handTracking.stop();
    }

    private void updateVibrationFeedback() {
        HandStatus currentStatus = handTracking.getHandStatus(pickingHand);

        if (currentStatus == HandStatus.UNKNOWN) {
            if (lastQoWStatus != HandStatus.UNKNOWN) {
                vibration.cancel();
            }
            lastQoWStatus = currentStatus;
            return;
        }

        lastQoWStatus = currentStatus;

        Position handPosition = handTracking.getAvgHandPosition(pickingHand);
        int qowScore = calculateQualityOfWorkScore(handPosition);

        vibration.vibrate((int) Math.round((qowScore / 100.0) * 255), qowScore);
    }


    private void updateDirection(double[] direction) {
        if(!isRunning) return;

        HandStatus newStatus = handTracking.getHandStatus(pickingHand);
        HandStatus cachedStatus = pickingHand == HandLabel.RIGHT ? rightHandStatus : leftHandStatus;

        if (!newStatus.equals(cachedStatus)) {
            if (pickingHand == HandLabel.RIGHT) rightHandStatus = newStatus;
            else leftHandStatus = newStatus;

            if (newStatus == HandStatus.UNKNOWN) {
                directionMqtt.sendUnknown();
                return;
            }
        }

        if (newStatus == HandStatus.UNKNOWN) {
            return;
        }

        directionMqtt.sendDirection(direction);
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

    private void showLiveLight(double[] direction, int qow) {
        if (!isRunning) return;

        if (handTracking.getHandStatus(pickingHand) == HandStatus.UNKNOWN) {
            if (!cachedBlueXRange.isEmpty()) {
                ledStrip.sendOFF(LEDStripClient.Clients.WS, LEDStripConfig.LEDStripId.X, cachedBlueXRange);
                cachedBlueXRange = Range.empty();
            }
            if (!cachedBlueYRange.isEmpty()) {
                ledStrip.sendOFF(LEDStripClient.Clients.WS, LEDStripConfig.LEDStripId.Y, cachedBlueYRange);
                cachedBlueYRange = Range.empty();
            }
            return;
        }

        Position avgHandPosition = handTracking.getAvgHandPosition(pickingHand);
        Position gridPosition = convertToStripCoords(avgHandPosition);

        int newXStart = (int) gridPosition.getX() - 2;
        int newXEnd   = (int) gridPosition.getX() + 2;
        int newYStart = (int) gridPosition.getY() - 2;
        int newYEnd   = (int) gridPosition.getY() + 2;

        Range newXRange = new Range(newXStart, newXEnd);
        Range newYRange = new Range(newYStart, newYEnd);

        if (!cachedBlueXRange.equalsRange(newXRange)) {
            ledStrip.sendOFF(LEDStripClient.Clients.WS, LEDStripConfig.LEDStripId.X, cachedBlueXRange);
            if (newXRange.resolvedOverlap(assemblyXRange)){
                ledStrip.sendDirectionalLight(LEDStripConfig.LEDStripId.X, newXRange, direction, qow);
            };
            cachedBlueXRange = newXRange;
        }

        if(!cachedBlueYRange.equalsRange(newYRange)) {
            ledStrip.sendOFF(LEDStripClient.Clients.WS, LEDStripConfig.LEDStripId.Y, cachedBlueYRange);
            if (newYRange.resolvedOverlap(assemblyYRange)){
                ledStrip.sendDirectionalLight(LEDStripConfig.LEDStripId.Y, newYRange, direction, qow);
            }
            cachedBlueYRange = newYRange;
        }
    }


    public boolean isCompleted() {return isCompleted.get();}
    public boolean isRunning() {return isRunning;}
}
