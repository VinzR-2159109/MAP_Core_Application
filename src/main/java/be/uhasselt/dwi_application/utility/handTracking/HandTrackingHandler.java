package be.uhasselt.dwi_application.utility.handTracking;

import be.uhasselt.dwi_application.model.hands.Hand;
import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.model.hands.Hands;
import be.uhasselt.dwi_application.model.hands.HandLabel;
import be.uhasselt.dwi_application.model.hands.HandStatus;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HandTrackingHandler {
    private static final String HAND_TOPIC = "sensor/hands/position";

    private final MqttHandler mqttHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicReference<Position> leftHandPosition = new AtomicReference<>(new Position(-1, -1));
    private final AtomicReference<Position> rightHandPosition = new AtomicReference<>(new Position(-1, -1));

    private HandStatus leftHandStatus = HandStatus.UNKNOWN;
    private HandStatus rightHandStatus = HandStatus.UNKNOWN;

    private Consumer<Position> leftHandListener;
    private Consumer<Position> rightHandListener;

    private static HandTrackingHandler instance;

    private HandTrackingHandler() {
        this.mqttHandler = MqttHandler.getInstance();
        subscribeToHandPositions();
        this.instance = this;
    }

    public static HandTrackingHandler getInstance() {
        if (instance == null) {
            instance = new HandTrackingHandler();
        }
        return instance;
    }

    private void subscribeToHandPositions() {
        System.out.println("Debug: Subscribing to hand positions");
        mqttHandler.subscribe(HAND_TOPIC, message -> {
            try {
                List<Hand> handsList = objectMapper.readValue(message, new TypeReference<>() {});
                Hands handsData = new Hands(handsList);
                updateHandPositions(handsData);
            } catch (Exception e) {
                System.err.println("Debug: Error parsing hand position JSON: " + e.getMessage());
            }
        });
    }

    private void updateHandPositions(Hands handsData) {
        boolean leftUpdated = false;
        boolean rightUpdated = false;

        for (Hand hand : handsData.getDetectedHands()) {
            Position newHandPos = hand.getPosition();
            HandStatus newHandStatus = hand.getStatus();

            if (hand.getLabel() == HandLabel.LEFT) {
                if (leftHandStatus != newHandStatus || !leftHandPosition.get().equals(newHandPos)) {
                    leftHandStatus = newHandStatus;
                    leftUpdated = true;
                }

                if (!leftHandStatus.equals(HandStatus.UNKNOWN)){
                    checkAndNotify(leftHandPosition, newHandPos, leftHandListener);
                }
            }

            else if (hand.getLabel() == HandLabel.RIGHT) {
                if (rightHandStatus != newHandStatus || !rightHandPosition.get().equals(newHandPos)) {
                    rightHandStatus = newHandStatus;
                    rightUpdated = true;
                }

                if (!rightHandStatus.equals(HandStatus.UNKNOWN)){
                    checkAndNotify(rightHandPosition, newHandPos, rightHandListener);

                }
            }
        }

        if (!leftUpdated && leftHandStatus != HandStatus.UNKNOWN) {
            leftHandStatus = HandStatus.UNKNOWN;
            leftHandPosition.set(new Position(-1, -1));
            if (leftHandListener != null) {
                leftHandListener.accept(leftHandPosition.get());
            }
        }

        if (!rightUpdated && rightHandStatus != HandStatus.UNKNOWN) {
            rightHandStatus = HandStatus.UNKNOWN;
            rightHandPosition.set(new Position(-1, -1));
            if (rightHandListener != null) {
                rightHandListener.accept(rightHandPosition.get());
            }
        }
    }

    private void checkAndNotify(AtomicReference<Position> storedPosition, Position newPosition, Consumer<Position> listener) {
        Position oldPosition = storedPosition.get();

        if (!oldPosition.equals(newPosition)) {
            storedPosition.set(newPosition);
            if (listener != null) {
                listener.accept(newPosition);
            }
        }
    }

    public void setLeftHandListener(Consumer<Position> listener) {
        this.leftHandListener = listener;
    }

    public void setRightHandListener(Consumer<Position> listener) {
        this.rightHandListener = listener;
    }

    public Position getLeftHandPosition() {
        return leftHandPosition.get();
    }

    public Position getRightHandPosition() {
        return rightHandPosition.get();
    }

    public HandStatus getLeftHandStatus() {
        return leftHandStatus;
    }

    public HandStatus getRightHandStatus() {
        return rightHandStatus;
    }

    public void stop() {
        rightHandPosition.set(new Position(-1,-1));
        rightHandStatus = HandStatus.UNKNOWN;

        leftHandPosition.set(new Position(-1, -1));
        leftHandStatus = HandStatus.UNKNOWN;

        mqttHandler.unsubscribe(HAND_TOPIC);
    }

    public void start() {
        subscribeToHandPositions();
    }
}
