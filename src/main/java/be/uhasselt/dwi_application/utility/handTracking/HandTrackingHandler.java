package be.uhasselt.dwi_application.utility.handTracking;

import be.uhasselt.dwi_application.model.hands.Hand;
import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.model.hands.Hands;
import be.uhasselt.dwi_application.model.hands.HandLabel;
import be.uhasselt.dwi_application.model.hands.HandStatus;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HandTrackingHandler {
    private static final String HAND_TOPIC = "sensor/hands/position";

    private final MqttHandler mqttHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicReference<Position> leftHandPosition = new AtomicReference<>(new Position(Integer.MIN_VALUE,Integer.MIN_VALUE));
    private final AtomicReference<Position> rightHandPosition = new AtomicReference<>(new Position(Integer.MIN_VALUE,Integer.MIN_VALUE));

    private HandStatus leftHandStatus = HandStatus.UNKNOWN;
    private HandStatus rightHandStatus = HandStatus.UNKNOWN;

    private static HandTrackingHandler instance;

    private HandTrackingHandler() {
        this.mqttHandler = MqttHandler.getInstance();
        instance = this;
    }

    public static HandTrackingHandler getInstance() {
        if (instance == null) {
            instance = new HandTrackingHandler();
        }
        return instance;
    }

    public void stop() {
        System.out.println("<Stopping Hand Tracking>");
        rightHandPosition.set(new Position(Integer.MIN_VALUE,Integer.MIN_VALUE));
        rightHandStatus = HandStatus.UNKNOWN;

        leftHandPosition.set(new Position(Integer.MIN_VALUE, Integer.MIN_VALUE));
        leftHandStatus = HandStatus.UNKNOWN;

        mqttHandler.unsubscribe(HAND_TOPIC);
    }

    public void start() {
        System.out.println("<Starting assembly instruction>");
        rightHandPosition.set(new Position(Integer.MIN_VALUE,Integer.MIN_VALUE));
        rightHandStatus = HandStatus.UNKNOWN;

        leftHandPosition.set(new Position(Integer.MIN_VALUE, Integer.MIN_VALUE));
        leftHandStatus = HandStatus.UNKNOWN;

        System.out.println("<Subscribing to hand positions>");
        mqttHandler.subscribe(HAND_TOPIC, message -> {
            try {
                List<Hand> handsList = objectMapper.readValue(message, new TypeReference<>() {});
                Hands handsData = new Hands(handsList);

                updateHandPositions(handsData);

            } catch (Exception e) {
                System.err.println("Error:: parsing hand position JSON: " + e.getMessage());
            }
        });
    }

    private void updateHandPositions(Hands handsData) {
        for (Hand hand : handsData.getDetectedHands()) {
            Position newHandPos = hand.getPosition();
            HandStatus newHandStatus = hand.getStatus();

            if (hand.getLabel() == HandLabel.LEFT) {
                if (leftHandStatus == HandStatus.UNKNOWN && leftHandStatus == newHandStatus) continue;

                if (leftHandStatus != newHandStatus || leftHandPosition.get() != newHandPos) {
                    leftHandStatus = newHandStatus;
                    leftHandPosition.set(newHandPos);
                }
            }

            else if (hand.getLabel() == HandLabel.RIGHT) {
                if (rightHandStatus == HandStatus.UNKNOWN && rightHandStatus == newHandStatus) continue;

                if (rightHandStatus != newHandStatus || rightHandPosition.get() != newHandPos) {
                    rightHandStatus = newHandStatus;
                    rightHandPosition.set(newHandPos);
                }
            }

        }
    }

    public Position getLeftHandPosition() {
        if (leftHandStatus == HandStatus.UNKNOWN) {
            return new Position(Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
        return leftHandPosition.get();
    }

    public Position getRightHandPosition() {
        if (rightHandStatus == HandStatus.UNKNOWN) {
            return new Position(Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
        return rightHandPosition.get();
    }

    public HandStatus getLeftHandStatus() {
        return leftHandStatus;
    }

    public HandStatus getRightHandStatus() {
        return rightHandStatus;
    }
}
