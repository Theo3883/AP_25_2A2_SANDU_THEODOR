package org.example;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class GameClient {
    private final String host;
    private final int port;
    private String clientId;
    private Socket socket;
    private PrintWriter out;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);

            // Start a thread to read server responses
            Thread serverListener = new Thread(this::listenToServer);
            serverListener.setDaemon(true);
            serverListener.start();

            // Main loop to read commands from keyboard
            Scanner scanner = new Scanner(System.in);
            System.out.println("Connected to server. Type 'help' for available commands or 'exit' to quit.");

            while (running.get()) {
                String input = scanner.nextLine();

                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
                out.println(input);
            }

        } catch (IOException e) {
            log.error("Error connecting to server: {}", e.getMessage());
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                log.error("Error closing connection", e);
            }
        }
    }

    private void listenToServer() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;

            while ((line = in.readLine()) != null) {
                if (line.startsWith("Connected to Hex Game Server. Your client ID:")) {
                    clientId = line.split("Your client ID: ")[1];
                    log.info("Received client ID: {}", clientId);
                }

                System.out.println("[Server]: " + line);

                if ("Server stopped".equals(line)) {
                    running.set(false);
                    System.out.println("Server has stopped. Press Enter to exit.");
                    break;
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                log.error("Connection to server lost: {}", e.getMessage());
                System.out.println("Connection to server lost. Press Enter to exit.");
                running.set(false);
            }
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