package org.example.game;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.player.PlayerState;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@Data
@Slf4j
public class HexGame {
    // Constants
    private static final int GAME_ID_LENGTH = 6;

    // Game configuration
    private final String gameId;
    private final int boardSize;
    private final long timeControlMillis;

    // Player information
    private String player1Id;
    private String player2Id;
    private long player1TimeRemaining;
    private long player2TimeRemaining;

    // Time tracking
    private Instant player1TimeStart;
    private Instant player2TimeStart;

    // Game state
    private boolean gameStarted;
    private boolean gameEnded;
    private String winner;
    @Getter
    private PlayerState currentPlayer;
    private Cell[][] board;

    private boolean aiGame = false; // Add this field to the HexGame class and its getter/setter methods

    public HexGame(int boardSize, long timeControlSeconds) {
        this.gameId = generateGameId();
        this.boardSize = boardSize;
        this.timeControlMillis = timeControlSeconds * 1000;
        this.player1TimeRemaining = timeControlMillis;
        this.player2TimeRemaining = timeControlMillis;
        this.currentPlayer = PlayerState.PLAYER1;

        initializeBoard();
    }

    private void initializeBoard() {
        this.board = new Cell[boardSize][boardSize];
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                board[row][col] = new Cell(row, col);
            }
        }
    }

    private String generateGameId() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }

    public boolean joinGame(String playerId) {
        if (player1Id == null) {
            player1Id = playerId;
            return true;
        } else if (player2Id == null && !player1Id.equals(playerId)) {
            player2Id = playerId;
            gameStarted = true;
            player1TimeStart = Instant.now();
            return true;
        }
        return false;
    }

    public boolean makeMove(String playerId, int row, int col) {
        if (!isValidGameState(playerId)) {
            return false;
        }

        updateTimeRemaining();
        if (hasPlayerTimedOut()) {
            return false;
        }
        if (!isValidMove(row, col)) {
            return false;
        }
        board[row][col].setOwner(currentPlayer);

        if (checkWinCondition(currentPlayer)) {
            gameEnded = true;
            winner = currentPlayer == PlayerState.PLAYER1 ? player1Id : player2Id;
        }
        switchCurrentPlayer();
        return true;
    }

    private boolean isValidGameState(String playerId) {
        if (!gameStarted || gameEnded) {
            return false;
        }
        return (currentPlayer == PlayerState.PLAYER1 && playerId.equals(player1Id)) ||
                (currentPlayer == PlayerState.PLAYER2 && playerId.equals(player2Id));
    }

    private boolean hasPlayerTimedOut() {
        if ((currentPlayer == PlayerState.PLAYER1 && player1TimeRemaining <= 0) ||
                (currentPlayer == PlayerState.PLAYER2 && player2TimeRemaining <= 0)) {
            gameEnded = true;
            winner = currentPlayer == PlayerState.PLAYER1 ? player2Id : player1Id;
            return true;
        }
        return false;
    }

    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < boardSize &&
                col >= 0 && col < boardSize &&
                board[row][col].getOwner() == PlayerState.EMPTY;
    }


    private void switchCurrentPlayer() {
        if (currentPlayer == PlayerState.PLAYER1) {
            player1TimeStart = null;
            player2TimeStart = Instant.now();
            currentPlayer = PlayerState.PLAYER2;
        } else {
            player2TimeStart = null;
            player1TimeStart = Instant.now();
            currentPlayer = PlayerState.PLAYER1;
        }
    }

    public void updateTimeRemaining() {
        updatePlayer1Time();
        updatePlayer2Time();
    }

    private void updatePlayer1Time() {
        if (player1TimeStart != null) {
            long elapsed = Duration.between(player1TimeStart, Instant.now()).toMillis();
            player1TimeRemaining -= elapsed;
            player1TimeStart = Instant.now();

            if (player1TimeRemaining <= 0) {
                player1TimeRemaining = 0;
                handleTimeout(PlayerState.PLAYER1);
            }
        }
    }

    private void updatePlayer2Time() {
        if (player2TimeStart != null) {
            long elapsed = Duration.between(player2TimeStart, Instant.now()).toMillis();
            player2TimeRemaining -= elapsed;
            player2TimeStart = Instant.now();

            if (player2TimeRemaining <= 0) {
                player2TimeRemaining = 0;
                handleTimeout(PlayerState.PLAYER2);
            }
        }
    }

    private void handleTimeout(PlayerState player) {
        if (!gameEnded) {
            gameEnded = true;

            if (player == PlayerState.PLAYER1) {
                winner = player2Id;
                log.info("Player 1's time has run out. Player 2 wins.");
            } else {
                winner = player1Id;
                log.info("Player 2's time has run out. Player 1 wins.");
            }
        }
    }

    private boolean checkWinCondition(PlayerState player) {
        DisjointSet ds = new DisjointSet(boardSize * boardSize + 2);
        int source = boardSize * boardSize;
        int target = boardSize * boardSize + 1;

        connectPlayerBorders(player, ds, source, target);
        connectPlayerCells(player, ds);

        return ds.find(source) == ds.find(target);
    }

    private void connectPlayerBorders(PlayerState player, DisjointSet ds, int source, int target) {
        if (player == PlayerState.PLAYER1) {
            // Player 1 connects left to right
            for (int i = 0; i < boardSize; i++) {
                if (board[i][0].getOwner() == player) {
                    ds.union(i * boardSize, source);
                }
                if (board[i][boardSize - 1].getOwner() == player) {
                    ds.union(i * boardSize + boardSize - 1, target);
                }
            }
            return;
        }

        // Player 2 connects top to bottom
        for (int j = 0; j < boardSize; j++) {
            if (board[0][j].getOwner() == player) {
                ds.union(j, source);
            }
            if (board[boardSize - 1][j].getOwner() == player) {
                ds.union((boardSize - 1) * boardSize + j, target);
            }
        }

    }

    private void connectPlayerCells(PlayerState player, DisjointSet ds) {
        // Hex board has 6 possible directions for connections
        int[][] directions = {{-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].getOwner() != player) continue;

                int cellIndex = i * boardSize + j;
                for (int[] dir : directions) {
                    int ni = i + dir[0];
                    int nj = j + dir[1];

                    if (ni >= 0 && ni < boardSize && nj >= 0 && nj < boardSize &&
                            board[ni][nj].getOwner() == player) {
                        int neighborIndex = ni * boardSize + nj;
                        ds.union(cellIndex, neighborIndex);
                    }
                }

            }
        }
    }

    public String getGameState() {
        updateTimeRemaining();
        return getGameInfo() + getBoardState();
    }

    public String getGameInfo() {
        updateTimeRemaining();

        StringBuilder info = new StringBuilder();
        info.append("Game ID: ").append(gameId).append("\n");
        info.append("Board Size: ").append(boardSize).append("x").append(boardSize).append("\n");
        info.append("Player 1: ").append(player1Id).append(" (Time: ").append(player1TimeRemaining / 1000).append("s)\n");
        info.append("Player 2: ").append(player2Id != null ? player2Id : "Waiting...").append(
                player2Id != null ? " (Time: " + player2TimeRemaining / 1000 + "s)\n" : "\n");
        info.append("Current player: ").append(currentPlayer == PlayerState.PLAYER1 ? "1" : "2").append("\n");
        info.append("Game status: ").append(gameEnded ? "Ended" : (gameStarted ? "Started" : "Waiting")).append("\n");

        if (gameEnded && winner != null) {
            info.append("Winner: ").append(winner).append("\n");
        }

        return info.toString();
    }

    public String getBoardState() {
        updateTimeRemaining();
        StringBuilder state = new StringBuilder();
        printColumnHeaders(state);
        printPlayerBorderTop(state);

        for (int i = 0; i < boardSize; i++) {
            state.append(" ".repeat(i));
            state.append(String.format("%2d ", i));
            state.append("X ");
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].getOwner() == PlayerState.EMPTY) {
                    state.append(". ");
                } else if (board[i][j].getOwner() == PlayerState.PLAYER1) {
                    state.append("X ");
                } else {
                    state.append("O ");
                }
            }

            state.append("X ");
            state.append(String.format("%d", i));
            state.append("\n");
        }

        printPlayerBorderBottom(state);
        return state.toString();
    }

    private void printColumnHeaders(StringBuilder state) {
        state.append("    ");
        for (int j = 0; j < boardSize; j++) {
            state.append(String.format("%2c", 'A' + j));
        }
        state.append("\n");
    }

    private void printPlayerBorderTop(StringBuilder state) {
        state.append("    ");
        state.append(" O".repeat(Math.max(0, boardSize)));
        state.append("\n");
    }

    private void printPlayerBorderBottom(StringBuilder state) {
        state.append(" ".repeat(Math.max(0, boardSize)));
        state.append("   ");
        state.append(" O".repeat(Math.max(0, boardSize)));
        state.append("\n");

        state.append(" ".repeat(Math.max(0, boardSize)));
        state.append("   ");
        for (int j = 0; j < boardSize; j++) {
            state.append(String.format("%2c", 'A' + j));
        }
        state.append("\n");
    }

    public String getPlayerBoardState(String playerId) {
        updateTimeRemaining();
        StringBuilder state = new StringBuilder();

        appendGameStatus(state, playerId);
        appendPlayerInfo(state, playerId);
        appendTurnInfo(state, playerId);
        state.append(getBoardState());

        return state.toString();
    }

    private void appendGameStatus(StringBuilder state, String playerId) {
        if (gameEnded) {
            state.append("GAME OVER! ");
            if (winner != null) {
                if (winner.equals(playerId)) {
                    state.append("You have won the game!\n");
                } else {
                    state.append("Your opponent has won the game.\n");
                }
            } else {
                state.append("The game has ended in a draw.\n");
            }
        }
    }


    private void appendPlayerInfo(StringBuilder state, String playerId) {
        if (playerId.equals(player1Id)) {
            state.append("You are Player 1 (X) - Connect LEFT to RIGHT\n");
            state.append("Your remaining time: ").append(Math.max(0, player1TimeRemaining / 1000)).append("s\n");
            state.append("Opponent's remaining time: ").append(Math.max(0, player2TimeRemaining / 1000)).append("s\n");
        } else if (playerId.equals(player2Id)) {
            state.append("You are Player 2 (O) - Connect TOP to BOTTOM\n");
            state.append("Your remaining time: ").append(Math.max(0, player2TimeRemaining / 1000)).append("s\n");
            state.append("Opponent's remaining time: ").append(Math.max(0, player1TimeRemaining / 1000)).append("s\n");
        }
    }


    private void appendTurnInfo(StringBuilder state, String playerId) {
        if (!gameEnded) {
            if ((currentPlayer == PlayerState.PLAYER1 && playerId.equals(player1Id)) ||
                    (currentPlayer == PlayerState.PLAYER2 && playerId.equals(player2Id))) {
                state.append("IT'S YOUR TURN!\n");
            } else {
                state.append("Waiting for opponent's move...\n");
            }
        }
    }

    public boolean isAiGame() {
        return aiGame;
    }

    public void setAiGame(boolean aiGame) {
        this.aiGame = aiGame;
    }

    public PlayerState getPlayer1State() {
        return PlayerState.PLAYER1;
    }

    public PlayerState getPlayer2State() {
        return PlayerState.PLAYER2;
    }
}
