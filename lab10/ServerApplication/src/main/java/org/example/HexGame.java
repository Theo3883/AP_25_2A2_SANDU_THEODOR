package org.example;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@Data
@Slf4j
public class HexGame {
    private final String gameId;
    private final int boardSize;
    private final long timeControlMillis; // time in milliseconds for each player

    private String player1Id;
    private String player2Id;
    private Instant player1TimeStart;
    private Instant player2TimeStart;
    private long player1TimeRemaining;
    private long player2TimeRemaining;
    private boolean gameStarted;
    private boolean gameEnded;
    private String winner;
    private PlayerState currentPlayer;
    private Cell[][] board;

    public HexGame(int boardSize, long timeControlSeconds) {
        this.gameId = generateGameId();
        this.boardSize = boardSize;
        this.timeControlMillis = timeControlSeconds * 1000;
        this.player1TimeRemaining = timeControlMillis;
        this.player2TimeRemaining = timeControlMillis;
        this.currentPlayer = PlayerState.PLAYER1;
        this.board = new Cell[boardSize][boardSize];

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = new Cell(i, j);
            }
        }
    }

    private String generateGameId() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000); // 6-digit number
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
        if (!gameStarted || gameEnded) {
            return false;
        }
        if ((currentPlayer == PlayerState.PLAYER1 && !playerId.equals(player1Id)) ||
                (currentPlayer == PlayerState.PLAYER2 && !playerId.equals(player2Id))) {
            return false;
        }

        updateTimeRemaining();

        if ((currentPlayer == PlayerState.PLAYER1 && player1TimeRemaining <= 0) ||
                (currentPlayer == PlayerState.PLAYER2 && player2TimeRemaining <= 0)) {
            gameEnded = true;
            winner = currentPlayer == PlayerState.PLAYER1 ? player2Id : player1Id;
            return false;
        }
        if (row < 0 || row >= boardSize || col < 0 || col >= boardSize ||
                board[row][col].getOwner() != PlayerState.EMPTY) {
            return false;
        }
        board[row][col].setOwner(currentPlayer);

        if (checkWinCondition(currentPlayer)) {
            gameEnded = true;
            winner = currentPlayer == PlayerState.PLAYER1 ? player1Id : player2Id;
        }

        if (currentPlayer == PlayerState.PLAYER1) {
            player1TimeStart = null;
            player2TimeStart = Instant.now();
            currentPlayer = PlayerState.PLAYER2;
        } else {
            player2TimeStart = null;
            player1TimeStart = Instant.now();
            currentPlayer = PlayerState.PLAYER1;
        }

        return true;
    }

    public void updateTimeRemaining() {
        if (player1TimeStart != null) {
            long elapsed = Duration.between(player1TimeStart, Instant.now()).toMillis();
            player1TimeRemaining -= elapsed;
            player1TimeStart = Instant.now();

            if (player1TimeRemaining <= 0) {
                player1TimeRemaining = 0;
                if (!gameEnded) {
                    gameEnded = true;
                    winner = player2Id;
                    log.info("Player 1's time has run out. Player 2 wins.");
                }
            }
        }

        if (player2TimeStart != null) {
            long elapsed = Duration.between(player2TimeStart, Instant.now()).toMillis();
            player2TimeRemaining -= elapsed;
            player2TimeStart = Instant.now();

            if (player2TimeRemaining <= 0) {
                player2TimeRemaining = 0;
                if (!gameEnded) {
                    gameEnded = true;
                    winner = player1Id;
                    log.info("Player 2's time has run out. Player 1 wins.");
                }
            }
        }
    }

    private boolean checkWinCondition(PlayerState player) {
        DisjointSet ds = new DisjointSet(boardSize * boardSize + 2);
        int source = boardSize * boardSize;
        int target = boardSize * boardSize + 1;

        if (player == PlayerState.PLAYER1) {
            for (int i = 0; i < boardSize; i++) {
                if (board[i][0].getOwner() == player) {
                    ds.union(i * boardSize, source);
                }
                if (board[i][boardSize - 1].getOwner() == player) {
                    ds.union(i * boardSize + boardSize - 1, target);
                }
            }
        } else {
            for (int j = 0; j < boardSize; j++) {
                if (board[0][j].getOwner() == player) {
                    ds.union(j, source);
                }
                if (board[boardSize - 1][j].getOwner() == player) {
                    ds.union((boardSize - 1) * boardSize + j, target);
                }
            }
        }

        int[][] directions = {{-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].getOwner() == player) {
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
        return ds.find(source) == ds.find(target);
    }

    public String getGameState() {
        updateTimeRemaining();
        StringBuilder state = new StringBuilder();
        state.append(getGameInfo());
        state.append(getBoardState());
        return state.toString();
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
        state.append("  ");
        for (int j = 0; j < boardSize; j++) {
            state.append(String.format("%2c", 'A' + j));
        }
        state.append("\n");

        for (int i = 0; i < boardSize; i++) {
            for (int s = 0; s < i; s++) {
                state.append(" ");
            }
            state.append(String.format("%2d ", i));

            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].getOwner() == PlayerState.EMPTY) {
                    state.append(". ");
                } else if (board[i][j].getOwner() == PlayerState.PLAYER1) {
                    state.append("X ");
                } else {
                    state.append("O ");
                }
            }
            state.append("\n");
        }

        return state.toString();
    }

    public String getPlayerBoardState(String playerId) {
        updateTimeRemaining();

        StringBuilder state = new StringBuilder();
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

        if (playerId.equals(player1Id)) {
            state.append("You are Player 1 (X)\n");
            state.append("Your remaining time: ").append(Math.max(0, player1TimeRemaining / 1000)).append("s\n");
            state.append("Opponent's remaining time: ").append(Math.max(0, player2TimeRemaining / 1000)).append("s\n");
        } else if (playerId.equals(player2Id)) {
            state.append("You are Player 2 (O)\n");
            state.append("Your remaining time: ").append(Math.max(0, player2TimeRemaining / 1000)).append("s\n");
            state.append("Opponent's remaining time: ").append(Math.max(0, player1TimeRemaining / 1000)).append("s\n");
        }

        if (!gameEnded) {
            if ((currentPlayer == PlayerState.PLAYER1 && playerId.equals(player1Id)) ||
                (currentPlayer == PlayerState.PLAYER2 && playerId.equals(player2Id))) {
                state.append("It's your turn!\n");
            } else {
                state.append("Waiting for opponent's move...\n");
            }
        }

        state.append(getBoardState());
        return state.toString();
    }
}
