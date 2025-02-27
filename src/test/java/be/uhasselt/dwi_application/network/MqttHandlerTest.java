package be.uhasselt.dwi_application.network;

import be.uhasselt.dwi_application.utility.network.MqttHandler;
import org.junit.jupiter.api.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MqttHandlerTest {

    private static final String TEST_TOPIC = "sensor/test";
    private static final String TEST_MESSAGE = "MQTT Test Message";

    private static MqttHandler mqttHandler;

    @BeforeAll
    static void setup() {
        mqttHandler = MqttHandler.getInstance();
        System.out.println("Connected to MQTT Broker for Testing.");
    }

    @Test
    @Order(1)
    void testPublishAndReceive() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture<String> receivedMessageFuture = new CompletableFuture<>();

        mqttHandler.subscribe(TEST_TOPIC, message -> {
            System.out.println("Received message: " + message);
            receivedMessageFuture.complete(message);
            latch.countDown();
        });

        mqttHandler.publish(TEST_TOPIC, TEST_MESSAGE);
        System.out.println("Test message published: " + TEST_MESSAGE);

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Message was not received within timeout.");

        assertEquals(TEST_MESSAGE, receivedMessageFuture.get(), "Received message does not match published message.");
    }
}