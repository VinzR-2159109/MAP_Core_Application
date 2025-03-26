package be.uhasselt.dwi_application.controller.AssemblyPlayer.Pick;

import be.uhasselt.dwi_application.model.Jackson.ObstacleSensorData;
import be.uhasselt.dwi_application.model.workInstruction.picking.PickingBin;
import be.uhasselt.dwi_application.model.workInstruction.picking.PickingInstruction;
import be.uhasselt.dwi_application.utility.database.repository.pickingBin.BinRepository;
import be.uhasselt.dwi_application.utility.exception.BinNotFoundException;
import be.uhasselt.dwi_application.utility.modules.SoundPlayer;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class PickInstructionHandler {
    private PickingBin bin;
    private Boolean isRunning;

    private static final String OBSTACLE_TOPIC = "Input/Bin/Obstacle";

    private final AtomicBoolean obstacleInBin;
    private final AtomicBoolean isCompleted;
    private final ObjectMapper objectMapper;
    private final PickMQTTHelper mqttHelper;

    private final MqttHandler mqttHandler = MqttHandler.getInstance();

    public enum PickingHand {
        LEFT, RIGHT;
    }

    public PickInstructionHandler(){
        this.objectMapper = new ObjectMapper();
        this.mqttHelper = new PickMQTTHelper();
        this.obstacleInBin = new AtomicBoolean(false);
        this.isCompleted = new AtomicBoolean(false);

        this.isRunning = false;
    }

    public void start(PickingInstruction pickingInstruction, Runnable onCompleteCallback) throws BinNotFoundException {
        System.out.println("<Starting Picking Instruction Assistance>");

        isRunning = true;

        obstacleInBin.set(false);
        isCompleted.set(false);

        try {
            this.bin = BinRepository.getInstance().getBinsByPartId(pickingInstruction.getPartId()).stream().findFirst()
                    .orElseThrow(() -> new BinNotFoundException(pickingInstruction.getPartToPick()));
        } catch (BinNotFoundException e) {
            System.err.println("Bin not found: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error fetching bin: " + e.getMessage());
        }

        mqttHelper.SendSetBinLEDGreen(bin.getId());
        mqttHelper.SendSetDisplayNumber(bin.getId().intValue(), pickingInstruction.getQuantity());

        mqttHandler.subscribe(OBSTACLE_TOPIC, s -> {
            try {
                ObstacleSensorData obstacleSensorData = objectMapper.readValue(s, ObstacleSensorData.class);
                if (Objects.equals(obstacleSensorData.id(), bin.getId())) {
                    boolean currentObstacle = obstacleSensorData.obstacle();

                    if (obstacleInBin.get() && !currentObstacle) {
                        isCompleted.set(true);
                        SoundPlayer.play(SoundPlayer.SoundType.OK);

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
        System.out.println("\u001B[31m" + "<Stopping PickInstructionHandler>" + "\u001B[0m");

        mqttHelper.SendSetBinLEDOff(bin.getId());
        mqttHelper.SendSetDisplayOff(bin.getId().intValue());

        mqttHandler.unsubscribe(OBSTACLE_TOPIC);

        isRunning = false;
    }

    public boolean isCompleted() {return isCompleted.get();}
    public boolean isRunning() {return isRunning;}
}
