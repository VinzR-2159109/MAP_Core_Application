package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip;

import be.uhasselt.dwi_application.utility.modules.ConsoleColors;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/liveLight")
public class LiveLightWebSocketEndpoint {
    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();

    public LiveLightWebSocketEndpoint() {}

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("Client connected: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("Client disconnected: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received: " + message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println(ConsoleColors.RED + "WebSocket Error: " + throwable.getMessage() + ConsoleColors.RESET);
    }

    public static void broadcast(String message) {
        for (Session session : sessions) {
            if (session.isOpen()) {
                session.getAsyncRemote().sendText(message);
            }
        }
    }
}
