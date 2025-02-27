package be.uhasselt.dwi_application.utility.network;

import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TCPConnectionHandler {
    private final String host;
    private final int port;
    private Socket socket;
    private InputStream inputStream;

    public TCPConnectionHandler(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            inputStream = socket.getInputStream();
            System.out.println("[TCP] Connected to " + host + ":" + port);
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] Unable to connect to " + host + ":" + port);
            return false;
        }
    }

    public byte[] readData() {
        try {
            byte[] sizeBytes = new byte[4];
            if (inputStream.read(sizeBytes) == -1) return null;
            int dataSize = ByteBuffer.wrap(sizeBytes).getInt();

            byte[] data = new byte[dataSize];
            int totalRead = 0;
            while (totalRead < dataSize) {
                int bytesRead = inputStream.read(data, totalRead, dataSize - totalRead);
                if (bytesRead == -1) return null;
                totalRead += bytesRead;
            }
            return data;
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to read data: " + e.getMessage());
            return null;
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void close() {
        try {
            if (socket != null) socket.close();
            System.out.println("[TCP] Connection closed.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to close connection.");
        }
    }
}
