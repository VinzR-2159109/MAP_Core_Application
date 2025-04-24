package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.Haptic;

import be.uhasselt.dwi_application.utility.modules.ConsoleColors;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws/haptic")
public class HapticWebSocketEndpoint {
    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();

    public HapticWebSocketEndpoint() {}

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println(ConsoleColors.GREEN + "[HAPTIC] Client connected: " + session.getId() + ConsoleColors.RESET);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println(ConsoleColors.YELLOW + "[HAPTIC] Client disconnected: " + session.getId() + ConsoleColors.RESET);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println(ConsoleColors.CYAN + "[HAPTIC] Received message: " + message + ConsoleColors.RESET);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println(ConsoleColors.RED + "[HAPTIC] WebSocket Error: " + throwable.getMessage() + ConsoleColors.RESET);
    }

    public static void broadcast(String message) {
        System.out.println(ConsoleColors.BLUE + "[HAPTIC] Broadcasting: " + message + ConsoleColors.RESET);
        for (Session session : sessions) {
            if (session.isOpen()) {
                session.getAsyncRemote().sendText(message);
            }
        }
    }
}
