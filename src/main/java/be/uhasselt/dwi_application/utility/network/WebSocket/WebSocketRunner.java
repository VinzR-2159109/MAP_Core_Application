package be.uhasselt.dwi_application.utility.network.WebSocket;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.Haptic.HapticWebSocketEndpoint;
import be.uhasselt.dwi_application.model.Jackson.Commands.Websocket.WebSocketCommand;
import be.uhasselt.dwi_application.utility.handTracking.HandsWebSocketEndpoint;
import be.uhasselt.dwi_application.utility.modules.ConsoleColors;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyClients.LEDStrip.LiveLightWebSocketEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.DeploymentException;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;


import static be.uhasselt.dwi_application.utility.network.NetworkUtil.getLocalIp;

public class WebSocketRunner {
    private static Server server;
    private final ObjectMapper objectMapper = new ObjectMapper();

    String URL = "ws://" + getLocalIp() + ":8080/ws";
    String LEDSTRIP_TOPIC = "Command/LEDStrip";
    String HANDTRACK_TOPIC = "Command/HandTracking";
    String HAPTIC_TOPIC = "Command/Haptic";

    public void connect() throws Exception {
        if (server != null && server.isRunning()) {
            System.out.println(ConsoleColors.RED + "<Server already running - Stopping>" + ConsoleColors.RESET);
            server.stop();
            server = null;
        }

        System.out.println(ConsoleColors.GREEN + "<Connecting " + LEDSTRIP_TOPIC + " on " + URL + ">" + ConsoleColors.RESET);

        server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        JakartaWebSocketServletContainerInitializer.configure(context, (_, serverContainer) -> {
            serverContainer.setDefaultMaxTextMessageBufferSize(128 * 1024);
            try {
                serverContainer.addEndpoint(LiveLightWebSocketEndpoint.class);
                serverContainer.addEndpoint(HandsWebSocketEndpoint.class);
                serverContainer.addEndpoint(HapticWebSocketEndpoint.class);

                serverContainer.setDefaultMaxSessionIdleTimeout(15 * 60 * 1000);
            } catch (DeploymentException e) {
                throw new RuntimeException(e);
            }
        });

        server.setHandler(context);
        server.start();

        WebSocketCommand ledConnect = WebSocketCommand.connect(URL, "liveLight");
        WebSocketCommand handTracking = WebSocketCommand.connect(URL, "hands");
        WebSocketCommand haptic = WebSocketCommand.connect(URL, "haptic");

        String jsonConnect_ledConnect = objectMapper.writeValueAsString(ledConnect);
        String jsonConnect_handTracking = objectMapper.writeValueAsString(handTracking);
        String jsonConnect_haptic = objectMapper.writeValueAsString(haptic);

        MqttHandler.getInstance().publish(LEDSTRIP_TOPIC, jsonConnect_ledConnect);
        MqttHandler.getInstance().publish(HANDTRACK_TOPIC, jsonConnect_handTracking);
        MqttHandler.getInstance().publish(HAPTIC_TOPIC, jsonConnect_haptic);
    }

}
