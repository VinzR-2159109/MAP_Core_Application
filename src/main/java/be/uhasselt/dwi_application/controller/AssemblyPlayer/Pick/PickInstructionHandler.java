package be.uhasselt.dwi_application.controller.AssemblyPlayer.Pick;

import be.uhasselt.dwi_application.model.Jackson.ObstacleSensorData;
import be.uhasselt.dwi_application.model.picking.PickingBin;
import be.uhasselt.dwi_application.model.workInstruction.PickingInstruction;
import be.uhasselt.dwi_application.utility.database.repository.pickingBin.BinRepository;
import be.uhasselt.dwi_application.utility.exception.BinNotFoundException;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

public class PickInstructionHandler {
    private PickingBin bin;
    private AtomicBoolean obstacleInBin;
    private AtomicBoolean isCompleted;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PickMQTTHelper mqttHelper = new PickMQTTHelper();

    private final MqttHandler mqttHandler = MqttHandler.getInstance();

    public enum PickingHand {
        LEFT, RIGHT;
    }

    public PickInstructionHandler() {
    }

    public void start(PickingInstruction pickingInstruction, Runnable onCompleteCallback) throws BinNotFoundException {
        System.out.println("<Starting Picking Instruction Assistance>");

        obstacleInBin = new AtomicBoolean(false);
        isCompleted = new AtomicBoolean(false);

        try {
            this.bin = BinRepository.getInstance().getBinsByPartId(pickingInstruction.getPartId()).stream().findFirst()
                    .orElseThrow(() -> new BinNotFoundException(pickingInstruction.getPartToPick()));
        } catch (BinNotFoundException e) {
            System.err.println("Bin not found: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error fetching bin: " + e.getMessage());
        }


        mqttHelper.SendSetBinLEDGreen(bin.getId());

        mqttHandler.subscribe("Input/Bin/Obstacle", s -> {
            try {
                ObstacleSensorData obstacleSensorData = objectMapper.readValue(s, ObstacleSensorData.class);
                if (Objects.equals(obstacleSensorData.id(), bin.getId())) {
                    boolean currentObstacle = obstacleSensorData.obstacle();

                    if (obstacleInBin.get() && !currentObstacle) {
                        isCompleted.set(true);
                        stop();
                        Platform.runLater(onCompleteCallback);
                    }

                    obstacleInBin.set(currentObstacle);
                }
                else if (obstacleSensorData.obstacle()) {
                    System.out.println("Wrong Bin Picked: " + bin.getId());
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        System.out.println("Stopping PickInstructionHandler");

        mqttHelper.SendSetBinLEDOff(bin.getId());

        // Unsubscribe from MQTT
        String topic = "Input/Bin/Obstacle";
        mqttHandler.unsubscribe(topic);
        System.out.println("Unsubscribed from " + topic);
    }

    public boolean isCompleted() {return isCompleted.get();};
}
