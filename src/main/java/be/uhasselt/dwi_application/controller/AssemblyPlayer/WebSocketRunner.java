package be.uhasselt.dwi_application.controller.AssemblyPlayer;

import be.uhasselt.dwi_application.model.Jackson.Commands.Websocket.WebSocketCommand;
import be.uhasselt.dwi_application.utility.modules.ConsoleColors;
import be.uhasselt.dwi_application.utility.network.MqttHandler;
import be.uhasselt.dwi_application.utility.network.WebSocket.WebSocketEndpoint;
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
                serverContainer.addEndpoint(WebSocketEndpoint.class);
                serverContainer.setDefaultMaxSessionIdleTimeout(15 * 60 * 1000);
            } catch (DeploymentException e) {
                throw new RuntimeException(e);
            }
        });

        server.setHandler(context);
        server.start();

        WebSocketCommand connect = WebSocketCommand.connect(URL);
        String jsonConnect = objectMapper.writeValueAsString(connect);
        MqttHandler.getInstance().publish(LEDSTRIP_TOPIC, jsonConnect);
    }

}
