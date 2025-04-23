package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.VisualDirectionMQTTHelper;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip.LEDStripClient;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.VibrationMQTTHelper;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.InstructionMeasurementHandler;
import be.uhasselt.dwi_application.model.Jackson.StripLedConfig.LEDStripConfig;
import be.uhasselt.dwi_application.model.Jackson.hands.HandLabel;
import be.uhasselt.dwi_application.model.basic.Color;
import be.uhasselt.dwi_application.model.basic.Range;
import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.model.Jackson.hands.HandStatus;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.utility.database.repository.settings.Settings;
import be.uhasselt.dwi_application.utility.database.repository.settings.SettingsRepository;
import be.uhasselt.dwi_application.utility.handTracking.HandTrackingHandler;
import be.uhasselt.dwi_application.utility.modules.ConsoleColors;
import be.uhasselt.dwi_application.utility.modules.SoundPlayer;
import javafx.application.Platform;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static be.uhasselt.dwi_application.utility.modules.ConvertToStripCoords.convertToStripCoords;

public class AssemblyInstructionHandler {
    private boolean isRunning;
    private final int gridSize;
    private final Settings settings = SettingsRepository.loadSettings();

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

        this.gridSize = SettingsRepository.loadSettings().getGridSize() / settings.getVideoEnlargementFactor();

        this.isRunning = false;

        this.ledStrip = new LEDStripClient(settings);
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
        int greenYStart = (int) positions.stream().mapToDouble(Position::getY).max().orElse(0) / gridSize;
        int greenYEnd   = (int) positions.stream().mapToDouble(Position::getY).min().orElse(0) / gridSize;

        assemblyXRange = new Range(greenXStart, greenXEnd);
        assemblyYRange = new Range(greenYEnd, greenYStart);
        
        if (settings.getEnabledAssistanceSystemsAsList().contains(Settings.EnabledAssistanceSystem.STATIC_LIGHT)){
            showStaticLight();
        }

        if (settings.getEnabledAssistanceSystemsAsList().contains(Settings.EnabledAssistanceSystem.FLOW_LIGHT)){
            showFlowLight();
        }

        this.measurement = new InstructionMeasurementHandler(assemblyInstruction.getAssembly(), assemblyInstruction, sessionId);
        measurement.startMeasurement();

        this.clk = new Timer();
        clk.schedule(new TimerTask() {
            @Override
            public void run() {
                Position avgHandPosition = handTracking.getAvgHandPosition(pickingHand);
                double[] direction = calculateDirectionToAssembly(avgHandPosition);
                double[] qow = calculateQualityOfWorkScore(avgHandPosition);

                if (settings.getEnabledAssistanceSystemsAsList().contains(Settings.EnabledAssistanceSystem.HAPTIC)){
                    updateDirection(direction);
                    updateVibrationFeedback();
                }

                if (qow[2] > settings.getNecessaryQOW()) {
                    isCompleted.set(true);
                    SoundPlayer.play(SoundPlayer.SoundType.OK);
                    vibration.cancel();
                    stop();
                    Platform.runLater(onCompleteCallback);
                }

                if (settings.getEnabledAssistanceSystemsAsList().contains(Settings.EnabledAssistanceSystem.LIVE_LIGHT)){
                    showLiveLight(direction, qow[0], qow[1]);
                }

            }
        }, 0, 10);

    }

    public void stop(){
        if (!isRunning) return;
        isRunning = false;

        System.out.println(ConsoleColors.RED + "<Stopping Assembly Instruction Assistance>" + ConsoleColors.RESET);

        measurement.stopMeasurement();

        ledStrip.stopFlowLight();
        ledStrip.sendAllOFF();
        vibration.cancel();

        if (clk != null){
            clk.cancel();
            clk.purge();
        }

        handTracking.stop();
    }

    private void showStaticLight() {
        if (!isRunning) return;
        System.out.println(ConsoleColors.GREEN + "<Setting Assembly Locations on LEDStrip>" + ConsoleColors.RESET);

        List<Integer> xIndices = IntStream.rangeClosed(assemblyXRange.start(), assemblyXRange.end()).boxed().toList();
        List<Integer> yIndices = IntStream.rangeClosed(assemblyYRange.start(), assemblyYRange.end()).boxed().toList();

        ledStrip.sendON(LEDStripConfig.LEDStripId.X, xIndices, Color.fromBasics(Color.BasicColors.GREEN));
        ledStrip.sendON(LEDStripConfig.LEDStripId.Y, yIndices, Color.fromBasics(Color.BasicColors.GREEN));
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
        double[] qow = calculateQualityOfWorkScore(handPosition);

        vibration.vibrate((int) Math.round((qow[2] / 100.0) * 255), qow[2]);
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

    private double[] calculateQualityOfWorkScore(Position handPosition) {
        if (handPosition == null) return new double[]{0, 0, 0};

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

        double dx = Math.abs(avgX - handPosition.getX());
        double dy = Math.abs(avgY - handPosition.getY());
        double distance = Math.sqrt(dx * dx + dy * dy);

        double maxDistance = 400.0;

        // Clamp distance to maxDistance to avoid negative scores
        dx = Math.min(dx, maxDistance);
        dy = Math.min(dy, maxDistance);
        distance = Math.min(distance, maxDistance);

        double qowX = 100.0 * (1.0 - dx / maxDistance);
        double qowY = 100.0 * (1.0 - dy / maxDistance);
        double qow  = 100.0 * (1.0 - distance / maxDistance);

        return new double[]{qowX, qowY, qow};
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

    private void showLiveLight(double[] direction, double qowX, double qowY) {
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

        int size = 3;
        int newXStart = (int) gridPosition.getX() - size;
        int newXEnd   = (int) gridPosition.getX() + size;
        int newYStart = (int) gridPosition.getY() - size;
        int newYEnd   = (int) gridPosition.getY() + size;

        Range newXRange = new Range(newXStart, newXEnd);
        Range newYRange = new Range(newYStart, newYEnd);

        if (!cachedBlueXRange.equalsRange(newXRange)) {
            ledStrip.sendOFF(LEDStripClient.Clients.WS, LEDStripConfig.LEDStripId.X, cachedBlueXRange);
            if (settings.getEnabledAssistanceSystemsAsList().contains(Settings.EnabledAssistanceSystem.STATIC_LIGHT)){
                if (newXRange.resolvedOverlap(assemblyXRange)){
                    ledStrip.sendLiveLight(LEDStripConfig.LEDStripId.X, newXRange, direction, qowX, qowY);
                };
            } else {
                ledStrip.sendLiveLight(LEDStripConfig.LEDStripId.X, newXRange, direction, qowX, qowY);
            }

            cachedBlueXRange = newXRange;
        }

        if(!cachedBlueYRange.equalsRange(newYRange)) {
            ledStrip.sendOFF(LEDStripClient.Clients.WS, LEDStripConfig.LEDStripId.Y, cachedBlueYRange);
            if (settings.getEnabledAssistanceSystemsAsList().contains(Settings.EnabledAssistanceSystem.STATIC_LIGHT)){
                if (newYRange.resolvedOverlap(assemblyYRange)) {
                    ledStrip.sendLiveLight(LEDStripConfig.LEDStripId.Y, newYRange, direction, qowX, qowY);
                }
            }
            else {
                ledStrip.sendLiveLight(LEDStripConfig.LEDStripId.Y, newYRange, direction, qowX, qowY);
            }
            cachedBlueYRange = newYRange;
        }
    }

    private void showFlowLight(){
        if (!isRunning) return;
        System.out.println(ConsoleColors.GREEN + "Showing flow light" + ConsoleColors.RESET);

        ledStrip.startFlowLight(LEDStripConfig.LEDStripId.X, assemblyXRange);
        ledStrip.startFlowLight(LEDStripConfig.LEDStripId.Y, assemblyYRange);
    }


    public boolean isCompleted() {return isCompleted.get();}
    public boolean isRunning() {return isRunning;}
}
