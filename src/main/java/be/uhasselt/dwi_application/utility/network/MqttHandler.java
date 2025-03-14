package be.uhasselt.dwi_application.utility.network;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;

public class MqttHandler {

    private static final String BROKER = "0f158df0574242429e54c7458f9f4a37.s1.eu.hivemq.cloud";
    private static final int PORT = 8883;
    private static final String USERNAME = "dwi_map";
    private static final String PASSWORD = "wRYx&RK%l5vsflnN";

    private static volatile MqttHandler instance;
    private final Mqtt5BlockingClient client;

    private MqttHandler() {
        client = MqttClient.builder()
                .useMqttVersion5()
                .identifier("DWI_Application")
                .serverHost(BROKER)
                .serverPort(PORT)
                .sslWithDefaultConfig()
                .buildBlocking();
        connect();
    }

    public static MqttHandler getInstance() {
        if (instance == null) {
            synchronized (MqttHandler.class) {
                if (instance == null) {
                    instance = new MqttHandler();
                }
            }
        }
        return instance;
    }

    private void connect() {
        client.connectWith()
                .simpleAuth()
                .username(USERNAME)
                .password(StandardCharsets.UTF_8.encode(PASSWORD))
                .applySimpleAuth()
                .send();
        System.out.println("Connected to MQTT Broker.");
    }

    public void subscribe(String topic, Consumer<String> callback) {
        client.subscribeWith()
                .topicFilter(topic)
                .send();

        client.toAsync().publishes(ALL, publish -> {
            String receivedTopic = publish.getTopic().toString();
            String payload = StandardCharsets.UTF_8.decode(publish.getPayload().get()).toString();

            if (receivedTopic.equals(topic)) {
                callback.accept(payload);
            }
        });
    }

    public void publish(String topic, String message) {
        client.publishWith()
                .topic(topic)
                .payload(StandardCharsets.UTF_8.encode(message))
                .send();
        System.out.println("Published to [" + topic + "]: " + message);
    }

    public void unsubscribe(String topic) {
        client.unsubscribeWith()
                .topicFilter(topic)
                .send();
        System.out.println("Unsubscribed from [" + topic + "]");
    }
}
