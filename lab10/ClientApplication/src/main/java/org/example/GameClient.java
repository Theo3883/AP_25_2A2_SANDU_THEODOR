package org.example;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class GameClient {
    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            connectToServer();
            startServerListenerThread();
            handleUserInput();
        } catch (IOException e) {
            log.error("Error connecting to server: {}", e.getMessage());
            System.out.println("Could not connect to server: " + e.getMessage());
        } finally {
            closeConnection();
            System.out.println("Disconnected from server.");
        }
    }

    private void connectToServer() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void startServerListenerThread() {
        Thread serverListener = new Thread(this::listenToServer);
        serverListener.setDaemon(true);
        serverListener.start();
    }

    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Connected to server. Type 'help' for available commands or 'exit' to quit.");

        while (running.get()) {
            String input = scanner.nextLine();
            if (shouldExit(input)) {
                break;
            }
            if (isConnectionBroken()) {
                System.out.println("Connection to server lost. Cannot send commands.");
                break;
            }
            sendCommandToServer(input);
        }
    }

    private boolean shouldExit(String input) {
        return "exit".equalsIgnoreCase(input);
    }

    private boolean isConnectionBroken() {
        return socket == null || socket.isClosed();
    }

    private void sendCommandToServer(String command) {
        try {
            out.println(command);
        } catch (Exception e) {
            System.out.println("Error sending command: " + e.getMessage());
            running.set(false);
        }
    }

    private void closeConnection() {
        running.set(false);
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            log.error("Error closing connection", e);
        }
    }

    private void listenToServer() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                processServerMessage(line);
            }
        } catch (SocketException e) {
            handleConnectionLost("Connection to server lost: " + e.getMessage());
        } catch (IOException e) {
            handleConnectionLost("Error communicating with server: " + e.getMessage());
        }
    }

    private void processServerMessage(String message) {
        String clientId;
        if (message.startsWith("Connected to Hex Game Server. Your client ID:")) {
            clientId = message.split("Your client ID: ")[1];
            log.info("Received client ID: {}", clientId);
        }
        System.out.println("[Server]: " + message);

        if ("Server stopped".equals(message)) {
            running.set(false);
            System.out.println("Server has stopped. Press Enter to exit.");
        }
    }

    private void handleConnectionLost(String errorMessage) {
        if (running.get()) {
            log.error(errorMessage);
            System.out.println(errorMessage + " Press Enter to exit.");
            running.set(false);
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 1234;

        if (args.length >= 2) {
            host = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                log.warn("Invalid port number, using default: 1234");
            }
        }
        new GameClient(host, port).start();
    }
}
