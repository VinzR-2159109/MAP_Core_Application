package be.uhasselt.dwi_application.utility.handTracking;

import be.uhasselt.dwi_application.model.Jackson.hands.Hand;
import be.uhasselt.dwi_application.model.Jackson.hands.Hands;
import be.uhasselt.dwi_application.utility.handTracking.HandTrackingHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.util.List;

@ServerEndpoint("/ws/hands")
public class HandsWebSocketEndpoint {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("🖐️ HandTracking WebSocket connected: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            List<Hand> hands = objectMapper.readValue(message, new TypeReference<>() {});
            Hands handsData = new Hands(hands);
            HandTrackingHandler.getInstance().updateFromWebSocket(handsData);
        } catch (Exception e) {
            System.err.println("❌ Failed to parse hands message: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("❌ HandTracking WebSocket disconnected: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("❌ WebSocket error on hands: " + throwable.getMessage());
    }
}
