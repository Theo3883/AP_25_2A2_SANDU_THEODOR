package org.example.server;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.game.GameManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class GameServer {
    @Getter
    private volatile boolean running = true;
    private final Set<ClientThread> clients = Collections.synchronizedSet(new HashSet<>());
    private final GameManager gameManager = new GameManager();
    private ServerSocket serverSocket;

    public void start(int port) {
        try{
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000); // Set timeout to allow checking if server is still running
            log.info("Server started on port {}", port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    log.info("New client connected: {}", clientSocket.getInetAddress());

                    ClientThread client = new ClientThread(clientSocket, this, gameManager);
                    clients.add(client);
                    client.start();
                } catch (SocketTimeoutException _) {
                    // This is expected due to the timeout - allows checking if server should continue running
                } catch (IOException e) {
                    if (running) {
                        log.error("Error accepting client connection", e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Server error", e);
        } finally {
            closeServerSocket();
        }
    }

    private void closeServerSocket() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.error("Error closing server socket", e);
            }
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
        closeServerSocket();
        gameManager.shutdown();
        clients.clear();
    }

    public void removeClient(ClientThread client) {
        clients.remove(client);
        log.info("Client disconnected, remaining clients: {}", clients.size());
    }

    public void notifyClient(String clientId, String message) {
        synchronized (clients) {
            for (ClientThread client : clients) {
                if (client.getClientId().equals(clientId)) {
                    client.sendMessage(message);
                    break;
                }
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