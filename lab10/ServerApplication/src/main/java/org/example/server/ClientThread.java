package org.example.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.game.GameManager;
import org.example.game.HexGame;
import org.example.commands.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class ClientThread extends Thread {
    private final Socket socket;
    private final GameServer server;
    private final GameManager gameManager;
    private PrintWriter out;
    @Getter private String clientId;
    @Getter @Setter private String currentGameId;
    private ScheduledExecutorService timeoutChecker;
    private Map<String, Command> commands;

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            setupClient();

            String line;
            while ((line = in.readLine()) != null && server.isRunning()) {
                log.info("Received message from client {}: {}", clientId, line);

                String response = processCommand(line);
                out.println(response);

                if (line.equalsIgnoreCase("stop")) {
                    log.info("Stop command received, shutting down server");
                    server.stop();
                    System.exit(0);
                    break;
                }
            }
        } catch (SocketException _) {
            log.info("Client {} disconnected abruptly", clientId);
        } catch (IOException e) {
            log.error("Error in client communication for client {}", clientId, e);
        } finally {
            handleClientDisconnect();
        }
    }

    private void setupClient() throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);

        clientId = UUID.randomUUID().toString();
        out.println("Connected to Hex Game Server. Your client ID: " + clientId);

        initializeCommands();
        startTimeoutChecker();
    }

    private void initializeCommands() {
        commands = new HashMap<>();

        Command createCmd = new CreateCommand(this, server, gameManager);
        Command joinCmd = new JoinCommand(this, server, gameManager);
        Command moveCmd = new MoveCommand(this, server, gameManager);
        Command stateCmd = new StateCommand(this, server, gameManager);
        Command listCmd = new ListCommand(this, server, gameManager);
        Command stopCmd = new StopCommand(this, server, gameManager);
        Command aiCmd = new AICommand(this, server, gameManager);

        commands.put("create", createCmd);
        commands.put("join", joinCmd);
        commands.put("move", moveCmd);
        commands.put("state", stateCmd);
        commands.put("list", listCmd);
        commands.put("stop", stopCmd);
        commands.put("ai", aiCmd);

        Command helpCmd = new HelpCommand(this, server, gameManager, commands);
        commands.put("help", helpCmd);
    }

    private void handleClientDisconnect() {
        if (currentGameId != null) {
            HexGame game = gameManager.getGame(currentGameId);
            if (game != null && game.isGameStarted() && !game.isGameEnded()) {
                String winnerId = determineWinner(game);
                
                game.setGameEnded(true);
                game.setWinner(winnerId);
                
                String message = "Your opponent has disconnected. You win the game!";
                server.notifyClient(winnerId, message);
                
                log.info("Player {} disconnected from game {}. Player {} wins.", 
                         clientId, currentGameId, winnerId);
            }
        }
        
        server.removeClient(this);
        stopTimeoutChecker();
        try {
            socket.close();
        } catch (IOException e) {
            log.debug("Error closing socket", e);
        }
    }

    private String determineWinner(HexGame game) {
        if (clientId.equals(game.getPlayer1Id())) {
            return game.getPlayer2Id();
        } else {
            return game.getPlayer1Id();
        }
    }

    private void startTimeoutChecker() {
        timeoutChecker = Executors.newSingleThreadScheduledExecutor();
        timeoutChecker.scheduleAtFixedRate(() -> {
            try {
                checkForTimeoutInCurrentGame();
            } catch (Exception e) {
                log.error("Error in timeout checker", e);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void stopTimeoutChecker() {
        if (timeoutChecker != null) {
            timeoutChecker.shutdown();
            try {
                if (!timeoutChecker.awaitTermination(1, TimeUnit.SECONDS)) {
                    timeoutChecker.shutdownNow();
                }
            } catch (InterruptedException _) {
                timeoutChecker.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void checkForTimeoutInCurrentGame() {
        if (currentGameId != null) {
            HexGame game = gameManager.getGame(currentGameId);
            if (game != null && game.isGameStarted() && !game.isGameEnded()) {
                game.updateTimeRemaining();

                if (game.isGameEnded() && game.getWinner() != null) {
                    notifyPlayersOfTimeout(game);
                }
            }
        }
    }
    

    private void notifyPlayersOfTimeout(HexGame game) {
        String timeoutMessage = "Game ended due to time expiration! " +
                "Player " + (game.getWinner().equals(game.getPlayer1Id()) ? "1" : "2") +
                " wins!\n" + game.getBoardState();

        server.notifyClient(game.getPlayer1Id(), timeoutMessage);
        server.notifyClient(game.getPlayer2Id(), timeoutMessage);
    }


    private String processCommand(String command) {
        String[] parts = command.split("\\s+");

        if (parts.length == 0) {
            return "Invalid command";
        }

        try {
            String commandName = parts[0].toLowerCase();
            Command cmd = commands.get(commandName);
            
            if (cmd != null) {
                return cmd.execute(parts);
            } else {
                return "Unknown command. Type 'help' for available commands.";
            }
        } catch (NumberFormatException _) {
            return "Invalid number format in command";
        } catch (Exception e) {
            log.error("Error processing command", e);
            return "Error processing command: " + e.getMessage();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println("[Server]: " + message);
        }
    }

    public void sendShutdownMessage() {
        if (out != null) {
            out.println("Server stopped");
        }
        try {
            socket.close();
        } catch (IOException e) {
            log.debug("Error closing socket during shutdown", e);
        }
    }
}
