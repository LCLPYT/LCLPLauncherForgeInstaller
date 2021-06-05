package work.lclpnet.forgeinstaller;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProgressCallbackClient {

    private static final List<Socket> openSockets = new ArrayList<>();

    private final String host;
    private final int port;
    private transient Socket socket;

    public ProgressCallbackClient(String host, int port) {
        this.host = host;
        this.port = port;
        openSocket();
    }

    private void openSocket() {
        try {
            socket = new Socket(host, port);
            openSockets.add(socket);
        } catch (IOException e) {
            System.err.println("Could not connect to '" + host + ":" + port + "'.");
            socket = null;
        }
    }

    private void send(byte[] bytes) {
        if (socket == null) return;
        try {
            socket.getOutputStream().write(bytes);
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
            IllegalStateException ex = new IllegalStateException("Error during sending progress");
            ex.addSuppressed(e);
            throw ex;
        }
    }

    public void setClientName(String name) {
        JsonObject obj = new JsonObject();
        obj.addProperty("setname", name);
        String s = obj + "\n";
        send(s.getBytes(StandardCharsets.UTF_8));
    }

    public void send(String msg) {
        JsonObject obj = new JsonObject();
        obj.addProperty("forgeInstaller", msg);
        msg = obj + "\n";
        send(msg.getBytes(StandardCharsets.UTF_8));
    }

    public static void closeAllSockets() {
        openSockets.forEach(s -> {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static boolean hasOpenSockets() {
        return !openSockets.isEmpty();
    }

}
