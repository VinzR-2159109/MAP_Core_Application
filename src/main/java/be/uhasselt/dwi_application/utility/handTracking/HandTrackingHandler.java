package be.uhasselt.dwi_application.utility.handTracking;

import be.uhasselt.dwi_application.model.Jackson.hands.*;
import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class HandTrackingHandler {
    private static final String HAND_TOPIC = "Input/HandsPosition";

    private final MqttHandler mqttHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicReference<Position> leftHandPosition = new AtomicReference<>(new Position(Integer.MIN_VALUE,Integer.MIN_VALUE));
    private final AtomicReference<Position> rightHandPosition = new AtomicReference<>(new Position(Integer.MIN_VALUE,Integer.MIN_VALUE));

    private final AtomicReference<Double> leftHandRotation = new AtomicReference<>(Double.MIN_VALUE);
    private final AtomicReference<Double> rightHandRotation = new AtomicReference<>(Double.MIN_VALUE);

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

                updateHandsPosition(handsData);
                updateHandRotation(handsData);

            } catch (Exception e) {
                System.err.println("Error:: parsing hand position JSON: " + e.getMessage());
            }
        });
    }

    private void updateHandsPosition(Hands handsData) {
        for (Hand hand : handsData.getDetectedHands()) {
            Position newHandPos = hand.getPosition();
            HandStatus newHandStatus = hand.getStatus();

            if (hand.getLabel() == HandLabel.LEFT) {
                if (leftHandStatus == HandStatus.UNKNOWN && leftHandStatus == newHandStatus) continue;

                if (leftHandStatus != newHandStatus || !leftHandPosition.get().equals(newHandPos)){
                    leftHandStatus = newHandStatus;
                    leftHandPosition.set(newHandPos);
                }
            }

            else if (hand.getLabel() == HandLabel.RIGHT) {
                if (rightHandStatus == HandStatus.UNKNOWN && rightHandStatus == newHandStatus) continue;

                if (rightHandStatus != newHandStatus || !rightHandPosition.get().equals(newHandPos)) {
                    rightHandStatus = newHandStatus;
                    rightHandPosition.set(newHandPos);
                }
            }

        }
    }

    private void updateHandRotation(Hands handsData) {
        for (Hand hand : handsData.getDetectedHands()) {
            List<LandmarkPosition> landmarks = hand.getLandmarks();

            LandmarkPosition wrist = null;
            LandmarkPosition middleTip = null;

            for (LandmarkPosition lm : landmarks) {
                if ("WRIST".equalsIgnoreCase(lm.getIndex())) {
                    wrist = lm;
                }
                else if ("MIDDLE_FINGER_TIP".equalsIgnoreCase(lm.getIndex())) {
                    middleTip = lm;
                }
            }

            if (wrist != null && middleTip != null) {
                int dx = middleTip.getX() - wrist.getX();
                int dy = middleTip.getY() - wrist.getY();

                double angleRadians = Math.atan2(dy, dx);
                double angleDegrees = Math.toDegrees(angleRadians);

                if (angleDegrees < 0) {
                    angleDegrees += 360;
                }

                if (hand.getLabel() == HandLabel.RIGHT) {
                    rightHandRotation.set(angleDegrees);
                } else if (hand.getLabel() == HandLabel.LEFT) {
                    leftHandRotation.set(angleDegrees);
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

    public Double getLeftHandRotation() {
        if (leftHandStatus == HandStatus.UNKNOWN) {
            return Double.MIN_VALUE;
        }
        return leftHandRotation.get();
    }

    public Double getRightHandRotation() {
        if (rightHandStatus == HandStatus.UNKNOWN) {
            return Double.MIN_VALUE;
        }
        return rightHandRotation.get();
    }


    public HandStatus getLeftHandStatus() {
        return leftHandStatus;
    }

    public HandStatus getRightHandStatus() {
        return rightHandStatus;
    }
}
