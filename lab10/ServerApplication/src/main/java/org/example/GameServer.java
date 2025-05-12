package org.example;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class GameServer {
    @Getter
    private volatile boolean running = true;
    private final Set<ClientThread> clients = Collections.synchronizedSet(new HashSet<>());
    private final GameManager gameManager = new GameManager();

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("Server started on port {}", port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                log.info("New client connected: {}", clientSocket.getInetAddress());

                ClientThread client = new ClientThread(clientSocket, this, gameManager);
                clients.add(client);
                client.start();
            }
        } catch (IOException e) {
            log.error("Server error", e);
        }
    }

    public void stop() {
        running = false;
        log.info("Stopping server...");
        synchronized (clients) {
            for (ClientThread client : clients) {
                client.sendShutdownMessage();
            }
        }
        clients.clear();
    }

    public void removeClient(ClientThread client) {
        clients.remove(client);
        log.info("Client disconnected, remaining clients: {}", clients.size());
    }

    public void notifyClient(String clientId, String message) {
        for (ClientThread client : clients) {
            if (client.getClientId().equals(clientId)) {
                client.sendMessage(message);
                break;
            }
        }
    }

    public static void main(String[] args) {
        int port = 1234;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                log.warn("Invalid port number, using default: 1234");
            }
        }
        new GameServer().start(port);
    }
}