package org.example.ai;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.game.Cell;
import org.example.game.HexGame;
import org.example.player.PlayerState;

import java.util.*;

@Slf4j
public class AIPlayer {
    @Getter
    private final String aiPlayerId;
    private final Random random = new Random();

    public AIPlayer(String aiPlayerId) {
        this.aiPlayerId = aiPlayerId;
    }


    public int[] makeMove(HexGame game) {
        log.info("AI is calculating next move");

        Cell[][] board = game.getBoard();
        int boardSize = game.getBoardSize();
        PlayerState aiPlayerState = game.getPlayer1Id().equals(aiPlayerId) ?
                PlayerState.PLAYER1 : PlayerState.PLAYER2;

        List<int[]> availableMoves = getAvailableMoves(board, boardSize);
        if (availableMoves.isEmpty()) {
            return new int[0];
        }


        if (isFirstMove(board, boardSize)) {
            return new int[]{boardSize / 2, boardSize / 2};
        }

        for (int[] move : availableMoves) {
            if (isWinningMove(board, boardSize, move, aiPlayerState)) {
                return move;
            }
        }

        // block opponent
        PlayerState opponentState = (aiPlayerState == PlayerState.PLAYER1) ?
                PlayerState.PLAYER2 : PlayerState.PLAYER1;
        for (int[] move : availableMoves) {
            if (isWinningMove(board, boardSize, move, opponentState)) {
                return move;
            }
        }

        // choose a move that could lead to a path
        int[] pathBuildingMove = findPathBuildingMove(board, boardSize, availableMoves, aiPlayerState);
        if (pathBuildingMove != null) {
            return pathBuildingMove;
        }


        return availableMoves.get(random.nextInt(availableMoves.size()));
    }


    private boolean isFirstMove(Cell[][] board, int boardSize) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].getOwner() != PlayerState.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }


    private List<int[]> getAvailableMoves(Cell[][] board, int boardSize) {
        List<int[]> moves = new ArrayList<>();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].getOwner() == PlayerState.EMPTY) {
                    moves.add(new int[]{i, j});
                }
            }
        }
        return moves;
    }


    private boolean isWinningMove(Cell[][] board, int boardSize, int[] move, PlayerState playerState) {
        Cell[][] tempBoard = new Cell[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                tempBoard[i][j] = new Cell(i, j);
                tempBoard[i][j].setOwner(board[i][j].getOwner());
            }
        }
        tempBoard[move[0]][move[1]].setOwner(playerState);
        return hasPath(tempBoard, boardSize, playerState);
    }


    private int[] findPathBuildingMove(Cell[][] board, int boardSize, List<int[]> availableMoves, PlayerState playerState) {
        // Prefer moves closer to existing pieces of the same player
        int[][] directions = {{-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};
        Map<int[], Integer> moveScores = new HashMap<>();

        for (int[] move : availableMoves) {
            int score = 0;
            int row = move[0];
            int col = move[1];

            // Check neighbors for friendly pieces
            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];

                if (newRow >= 0 && newRow < boardSize && newCol >= 0 && newCol < boardSize && board[newRow][newCol].getOwner() == playerState) {
                    score += 5;
                }

            }

            int centerDistance = Math.abs(row - boardSize / 2) + Math.abs(col - boardSize / 2);
            score += boardSize - centerDistance;

            Cell[][] tempBoard = new Cell[boardSize][boardSize];
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    tempBoard[i][j] = new Cell(i, j);
                    tempBoard[i][j].setOwner(board[i][j].getOwner());
                }
            }
            tempBoard[row][col].setOwner(playerState);

            if (hasPotentialPath(tempBoard, boardSize, playerState)) {
                score += 50;
            }

            moveScores.put(move, score);
        }

        return moveScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }


    private boolean hasPath(Cell[][] board, int boardSize, PlayerState player) {
        boolean[][] visited = new boolean[boardSize][boardSize];

        if (player == PlayerState.PLAYER1) {
            // Player 1 connects left to right
            for (int i = 0; i < boardSize; i++) {
                if (board[i][0].getOwner() == player && dfsCheckPath(board, boardSize, i, 0, visited, player, true)) {
                    return true;
                }

            }
        } else {
            // Player 2 connects top to bottom
            for (int j = 0; j < boardSize; j++) {
                if (board[0][j].getOwner() == player && dfsCheckPath(board, boardSize, 0, j, visited, player, false)) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean dfsCheckPath(Cell[][] board, int boardSize, int row, int col,
                                 boolean[][] visited, PlayerState player, boolean isHorizontal) {
        // Check if we reached the opposite edge
        if ((isHorizontal && col == boardSize - 1) || (!isHorizontal && row == boardSize - 1)) {
            return board[row][col].getOwner() == player;
        }

        visited[row][col] = true;

        // Hex board neighbors: top-left, top-right, left, right, bottom-left, bottom-right
        int[][] directions = {{-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (isValidCell(newRow, newCol, boardSize)
                    && !visited[newRow][newCol]
                    && board[newRow][newCol].getOwner() == player
                    && dfsCheckPath(board, boardSize, newRow, newCol, visited, player, isHorizontal)) {
                return true;
            }

        }

        return false;
    }


    private boolean hasPotentialPath(Cell[][] board, int boardSize, PlayerState player) {
        boolean[][] visited = new boolean[boardSize][boardSize];

        if (player == PlayerState.PLAYER1) {
            // Check for potential left-right path
            for (int i = 0; i < boardSize; i++) {
                if ((board[i][0].getOwner() == player || board[i][0].getOwner() == PlayerState.EMPTY)
                        && !visited[i][0] && dfsPotentialPath(board, boardSize, i, 0, visited, player, true)) {
                    return true;
                }

            }
        } else {
            // Check for potential top-bottom path
            for (int j = 0; j < boardSize; j++) {
                if ((board[0][j].getOwner() == player || board[0][j].getOwner() == PlayerState.EMPTY)
                        && !visited[0][j] && dfsPotentialPath(board, boardSize, 0, j, visited, player, false)) {
                    return true;
                }

            }
        }

        return false;
    }


    private boolean dfsPotentialPath(Cell[][] board, int boardSize, int row, int col,
                                     boolean[][] visited, PlayerState player, boolean isHorizontal) {
        // Check if we reached the opposite edge
        if ((isHorizontal && col == boardSize - 1) || (!isHorizontal && row == boardSize - 1)) {
            return board[row][col].getOwner() == player || board[row][col].getOwner() == PlayerState.EMPTY;
        }

        visited[row][col] = true;

        int[][] directions = {{-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (isValidCell(newRow, newCol, boardSize) &&
                    !visited[newRow][newCol] &&
                    (board[newRow][newCol].getOwner() == player || board[newRow][newCol].getOwner() == PlayerState.EMPTY)
                    && dfsPotentialPath(board, boardSize, newRow, newCol, visited, player, isHorizontal)) {
                return true;
            }

        }

        return false;
    }

    private boolean isValidCell(int row, int col, int boardSize) {
        return row >= 0 && row < boardSize && col >= 0 && col < boardSize;
    }
}