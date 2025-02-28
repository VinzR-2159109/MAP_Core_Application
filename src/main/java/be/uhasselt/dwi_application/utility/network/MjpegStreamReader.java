package be.uhasselt.dwi_application.utility.network;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MjpegStreamReader {
    private final String streamUrl;
    private boolean running;
    private ExecutorService executor;

    public MjpegStreamReader(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public void start(FrameListener listener) {
        running = true;
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> streamMJPEG(listener));
    }

    public void stop() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void streamMJPEG(FrameListener listener) {
        while (running) {
            try {
                URL url = new URI(streamUrl).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "JavaFX MJPEG Viewer");
                connection.setReadTimeout(5000);
                connection.connect();

                try (InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
                    while (running) {
                        Image fxImage = readFrameAsImage(inputStream);
                        if (fxImage != null) {
                            listener.onFrameReceived(fxImage);
                        }
                    }
                }
            } catch (IOException | ImageReadException | URISyntaxException e) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private Image readFrameAsImage(InputStream inputStream) throws IOException, ImageReadException {
        ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        boolean readingImage = false;
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            if (containsJPEGStart(buffer, bytesRead)) {
                imageBuffer.reset();
                readingImage = true;
            }

            if (readingImage) {
                imageBuffer.write(buffer, 0, bytesRead);
                if (containsJPEGEnd(buffer, bytesRead)) {
                    break;
                }
            }
        }

        byte[] jpegBytes = extractJpegBytes(imageBuffer.toByteArray());
        if (jpegBytes.length == 0) {
            return null;
        }

        BufferedImage bufferedImage = Imaging.getBufferedImage(jpegBytes);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    private boolean containsJPEGStart(byte[] data, int length) {
        for (int i = 0; i < length - 1; i++) {
            if (data[i] == (byte) 0xFF && data[i + 1] == (byte) 0xD8) {
                return true;
            }
        }
        return false;
    }

    private boolean containsJPEGEnd(byte[] data, int length) {
        for (int i = 0; i < length - 1; i++) {
            if (data[i] == (byte) 0xFF && data[i + 1] == (byte) 0xD9) {
                return true;
            }
        }
        return false;
    }

    private byte[] extractJpegBytes(byte[] rawData) {
        int start = -1, end = -1;

        for (int i = 0; i < rawData.length - 1; i++) {
            if (rawData[i] == (byte) 0xFF && rawData[i + 1] == (byte) 0xD8) {
                start = i;
                break;
            }
        }

        for (int i = rawData.length - 2; i > 0; i--) {
            if (rawData[i] == (byte) 0xFF && rawData[i + 1] == (byte) 0xD9) {
                end = i + 2;
                break;
            }
        }

        return (start != -1 && end != -1 && start < end) ? Arrays.copyOfRange(rawData, start, end) : new byte[0];
    }

    public interface FrameListener {
        void onFrameReceived(Image image);
    }
}
