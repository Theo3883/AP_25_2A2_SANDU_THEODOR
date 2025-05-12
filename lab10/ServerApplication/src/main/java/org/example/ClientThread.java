package org.example;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
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
    @Getter
    private String clientId;
    @Getter
    private String currentGameId;
    private ScheduledExecutorService timeoutChecker;

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);
            clientId = UUID.randomUUID().toString();
            out.println("Connected to Hex Game Server. Your client ID: " + clientId);
            startTimeoutChecker();

            String line;
            while ((line = in.readLine()) != null && server.isRunning()) {
                log.info("Received message from client {}: {}", clientId, line);

                String response = processCommand(line);
                out.println(response);

                if ("stop".equalsIgnoreCase(line)) {
                    log.info("Stop command received, shutting down server");
                    server.stop();
                    System.exit(0);
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Error in client communication", e);
        } finally {
            server.removeClient(this);
            stopTimeoutChecker();
            try {
                socket.close();
            } catch (IOException e) {
                log.debug("Error closing socket", e);
            }
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
        }
    }

    private void checkForTimeoutInCurrentGame() {
        if (currentGameId != null) {
            HexGame game = gameManager.getGame(currentGameId);
            if (game != null && game.isGameStarted() && !game.isGameEnded()) {
                game.updateTimeRemaining();

                if (game.isGameEnded() && game.getWinner() != null) {
                    String timeoutMessage = "Game ended due to time expiration! " +
                            "Player " + (game.getWinner().equals(game.getPlayer1Id()) ? "1" : "2") +
                            " wins!\n" + game.getBoardState();

                    server.notifyClient(game.getPlayer1Id(), timeoutMessage);
                    server.notifyClient(game.getPlayer2Id(), timeoutMessage);
                }
            }
        }
    }

    private String processCommand(String command) {
        String[] parts = command.split("\\s+");

        if (parts.length == 0) {
            return "Invalid command";
        }

        try {
            return switch (parts[0].toLowerCase()) {
                case "create" -> createCommand(parts);
                case "join" -> joinCommand(parts);
                case "move" -> moveCommand(parts);
                case "state" -> stateCommand(parts);
                case "list" -> listCommand();
                case "help" -> helpCommand();
                case "stop" -> "Server stopped";
                default -> "Unknown command. Type 'help' for available commands.";
            };
        } catch (NumberFormatException e) {
            return "Invalid number format in command";
        } catch (Exception e) {
            log.error("Error processing command", e);
            return "Error processing command: " + e.getMessage();
        }
    }

    private static String helpCommand() {
        return """
                Available commands:
                create <board_size> <time_control_seconds> - Create a new game
                join <game_id> - Join an existing game
                move <row> <col> - Make a move (row can be a number, column can be a letter A-Z)
                state - Get current game state after joining a game
                list - List all active games
                help - Show this help
                stop - Stop the server""";
    }

    private String listCommand() {
        StringBuilder gameList = new StringBuilder("Active games:\n");
        gameManager.getActiveGames().forEach((id, game) -> {
            gameList.append("Game ID: ").append(id)
                    .append(", Size: ").append(game.getBoardSize())
                    .append(", Status: ").append(game.isGameEnded() ? "Ended" :
                            (game.isGameStarted() ? "In progress" : "Waiting for players"))
                    .append("\n");
        });
        return gameList.toString();
    }

    private String stateCommand(String[] parts) {
        String gameId;

        if (parts.length >= 2) {
            gameId = parts[1];
        } else {
            gameId = findGameForClient();
            if (gameId == null) {
                return "You are not currently in any game. Please join a game first.";
            }
        }

        HexGame game = gameManager.getGame(gameId);
        if (game != null) {
            game.updateTimeRemaining();

            if (game.isGameEnded() && !parts[0].equalsIgnoreCase("move")) {
                if (game.getWinner() != null) {
                    String winnerMsg = "Game has ended. Player " +
                            (game.getWinner().equals(game.getPlayer1Id()) ? "1" : "2") +
                            " (" + (game.getWinner().equals(game.getPlayer1Id()) ? "X" : "O") + ") has won!";
                    return winnerMsg + "\n" + game.getBoardState();
                }
            }

            if (!game.isGameStarted()) {
                return game.getGameInfo();
            }
            return game.getPlayerBoardState(clientId);
        } else {
            return "Game not found";
        }
    }

    private String moveCommand(String[] parts) {
        if (parts.length >= 3) {
            String gameId;
            int row, col;

            try {
                gameId = findGameForClient();
                if (gameId == null) {
                    return "You are not currently in any game. Please join a game first.";
                }
                row = parseRowOrColumn(parts[1]);
                col = parseRowOrColumn(parts[2]);

                this.currentGameId = gameId;

                HexGame game = gameManager.getGame(gameId);
                if (game == null) {
                    return "Game not found";
                }

                game.updateTimeRemaining();
                if (game.isGameEnded()) {
                    String timeoutMsg = "Cannot make a move. Game has ended. ";
                    if (game.getWinner() != null) {
                        timeoutMsg += "Player " +
                                (game.getWinner().equals(game.getPlayer1Id()) ? "1" : "2") +
                                " (" + (game.getWinner().equals(game.getPlayer1Id()) ? "X" : "O") + ") has won!";
                    }
                    return timeoutMsg + "\n" + game.getBoardState();
                }

                boolean moveSuccessful = gameManager.makeMove(gameId, clientId, row, col);

                if (moveSuccessful) {
                    if (game.isGameEnded() && game.getWinner() != null) {
                        String winMessage = "Game Over! Player " +
                                (game.getWinner().equals(game.getPlayer1Id()) ? "1 (X)" : "2 (O)") +
                                " has won the game!\n" + game.getBoardState();

                        String opponentId = clientId.equals(game.getPlayer1Id()) ?
                                game.getPlayer2Id() : game.getPlayer1Id();

                        server.notifyClient(opponentId, winMessage);

                        if (game.getWinner().equals(clientId)) {
                            return "Congratulations! You have won the game!\n" + game.getBoardState();
                        } else {
                            return "Game Over! Your opponent has won the game.\n" + game.getBoardState();
                        }
                    }
                    else {
                        String opponentId = clientId.equals(game.getPlayer1Id()) ?
                                game.getPlayer2Id() : game.getPlayer1Id();

                        String updatedBoard = game.getPlayerBoardState(opponentId);
                        server.notifyClient(opponentId, "Opponent made a move. Your turn now!\n" + updatedBoard);

                        return "Move accepted:\n" + game.getPlayerBoardState(clientId);
                    }
                } else {
                    if (game.isGameEnded() && game.getWinner() != null) {
                        String timeoutMsg = "Time has expired! Player " +
                                (game.getWinner().equals(game.getPlayer1Id()) ? "1" : "2") +
                                " (" + (game.getWinner().equals(game.getPlayer1Id()) ? "X" : "O") + ") has won!";
                        return timeoutMsg + "\n" + game.getBoardState();
                    }

                    return "Invalid move:\n" + game.getPlayerBoardState(clientId);
                }
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }
        } else {
            return "Usage: move <row> <col> (row can be a number, column can be a letter A-Z)";
        }
    }

    private int parseRowOrColumn(String input) {
        input = input.trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            if (input.length() == 1) {
                char c = Character.toUpperCase(input.charAt(0));
                if (c >= 'A' && c <= 'Z') {
                    return c - 'A';
                }
            }
            throw new IllegalArgumentException("Invalid row/column format. Use numbers or letters A-Z.");
        }
    }

    private boolean isValidGameId(String id) {
        return id.matches("\\d{6}");
    }

    private String joinCommand(String[] parts) {
        if (parts.length < 2) {
            return "Usage: join <game_id>";
        }

        String gameId = parts[1];
        if (gameManager.joinGame(gameId, clientId)) {
            HexGame game = gameManager.getGame(gameId);
            String creatorId = game.getPlayer1Id();
            this.currentGameId = gameId;

            if (!clientId.equals(creatorId)) {
                server.notifyClient(creatorId, "Player joined your game: " + gameId + "\n" +
                        "Game starting! You are Player 1 (X). You go first!\n" +
                        game.getPlayerBoardState(creatorId));
                return "Joined game successfully! You are Player 2 (O). Wait for Player 1's move.\n" +
                        game.getPlayerBoardState(clientId);
            }

            return "Joined game successfully. Waiting for another player to join.\n" +
                    game.getGameInfo();
        } else {
            return "Failed to join game. Game might be full or not exist.";
        }
    }

    private String createCommand(String[] parts) {
        if (parts.length >= 3) {
            int boardSize = Integer.parseInt(parts[1]);
            long timeControl = Long.parseLong(parts[2]);

            if (boardSize < 5 || boardSize > 19) {
                return "Invalid board size. Choose between 5 and 19.";
            }

            if (timeControl < 30 || timeControl > 3600) {
                return "Invalid time control. Choose between 30 and 3600 seconds.";
            }

            HexGame game = gameManager.createGame(boardSize, timeControl);
            game.joinGame(clientId);
            this.currentGameId = game.getGameId();

            return "Game created successfully. Game ID: " + game.getGameId() + "\n" +
                   "You are Player 1 (X). Waiting for another player to join.\n" +
                   game.getGameInfo();
        } else {
            return "Usage: create <board_size> <time_control_seconds>";
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

    private String findGameForClient() {
        for (HexGame game : gameManager.getActiveGames().values()) {
            if (clientId.equals(game.getPlayer1Id()) || clientId.equals(game.getPlayer2Id())) {
                return game.getGameId();
            }
        }
        return null;
    }
}
