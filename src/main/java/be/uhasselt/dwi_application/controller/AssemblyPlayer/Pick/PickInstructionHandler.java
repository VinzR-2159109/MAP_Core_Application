package be.uhasselt.dwi_application.controller.AssemblyPlayer.Pick;

import be.uhasselt.dwi_application.model.Jackson.ObstacleSensorData;
import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.model.picking.PickingBin;
import be.uhasselt.dwi_application.model.workInstruction.PickingInstruction;
import be.uhasselt.dwi_application.utility.database.repository.pickingBin.BinRepository;
import be.uhasselt.dwi_application.utility.exception.BinNotFoundException;
import be.uhasselt.dwi_application.utility.handTracking.HandTrackingHandler;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class PickInstructionHandler {
    private final HandTrackingHandler handTracker = HandTrackingHandler.getInstance();
    private final MqttHandler mqttHandler = MqttHandler.getInstance();
    private final PickingHand pickingHand;
    private PickingBin bin;
    private Timer timer;
    private boolean isRunning = false; // Track execution state
    private final AtomicBoolean obstacleInBin = new AtomicBoolean(false);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PickMQTTHelper mqttHelper = new PickMQTTHelper();
    public enum PickingHand {
        LEFT, RIGHT;
    }

    public PickInstructionHandler(PickingHand pickingHand) {
        this.pickingHand = pickingHand;
        this.bin = null;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void start(PickingInstruction pickingInstruction, Runnable onCompleteCallback) throws BinNotFoundException {
        if (isRunning) {stop();} ; System.out.println("Starting picking instruction");

        // Getting Bin
        try {
            List<PickingBin> bins = BinRepository.getInstance().getBinsByPartId(pickingInstruction.getPartId());

            if (bins.isEmpty()) {
                System.err.println("Error: No bin found for part ID " + pickingInstruction.getPartId());
                throw new BinNotFoundException(pickingInstruction.getPartToPick());
            }

            this.bin = bins.getFirst();
        } catch (BinNotFoundException e) {
            System.err.println("Bin not found: " + e.getMessage());
            return;
        } catch (Exception e) {
            System.err.println("Unexpected error fetching bin: " + e.getMessage());
            return;
        }

        mqttHelper.SendSetBinLEDGreen(bin.getId());
        // Receiving Obstacle Detection
        String topic = "Input/Bin/Obstacle";
        mqttHandler.subscribe(topic, s -> {
            try {
                ObstacleSensorData obstacleSensorData = objectMapper.readValue(s, ObstacleSensorData.class);
                System.out.println("ObstacleSensorData: " + obstacleSensorData);

                if (Objects.equals(obstacleSensorData.id(), bin.getId())){
                    obstacleInBin.set(obstacleSensorData.obstacle());
                    System.out.println("Obstacle update for Bin " + bin.getId() + ": " + obstacleSensorData.obstacle());
                } else if (Objects.equals(obstacleSensorData.obstacle(), true)) {
                    //HandleWrongBinPicked
                    System.out.println("Wrong Bin Picked: " + bin.getId());
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        isRunning = true;
        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isRunning) return; // Prevent execution after stopping

                if (isPickCompleted(calculateQualityOfWorkScore())) {
                    System.out.println("Pick Completed");
                    stop();
                    Platform.runLater(onCompleteCallback);
                }
            }
        }, 0, 500);
    }

    public void stop() {
        if (!isRunning) return;
        System.out.println("Stopping PickInstructionHandler");

        isRunning = false;
        obstacleInBin.set(false);

        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        mqttHelper.SendSetBinLEDOff(bin.getId());

        // Unsubscribe from MQTT
        String topic = "Input/Bin/Obstacle";
        mqttHandler.unsubscribe(topic);
        System.out.println("Unsubscribed from " + topic);
    }

    private double calculateQualityOfWorkScore() {
        if (bin == null) {
            throw new IllegalStateException("Bin is null - did you call start() first?");
        }

        Position handPosition = pickingHand == PickingHand.LEFT ? handTracker.getLeftHandPosition() : handTracker.getRightHandPosition();
        if (handPosition == null) {
            throw new IllegalStateException("Hand position is null!");
        }

        Position binPosition = new Position(bin.getPos_x(), bin.getPos_y());
        double distance = Math.sqrt(Math.pow(binPosition.getX() - handPosition.getX(), 2) + Math.pow(binPosition.getY() - handPosition.getY(), 2));
        System.out.println("Distance: " + distance);

        double maxDistance = 550.0;
        double maxScore = 100;

        return Math.max(0, Math.min(maxScore, maxScore - (distance / maxDistance) * maxScore));
    }

    private boolean isPickCompleted(double qualityOfWorkScore) {
        boolean binClear = obstacleInBin.get();
        System.out.println("Quality of work score: " + qualityOfWorkScore + ", Bin clear: " + binClear);
        return qualityOfWorkScore > 85 && binClear;
    }
}
