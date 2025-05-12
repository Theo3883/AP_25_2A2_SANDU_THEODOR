package org.example.commands;

import org.example.server.ClientThread;
import org.example.game.GameManager;
import org.example.server.GameServer;
import org.example.game.HexGame;
import org.example.util.InputParser;

public class MoveCommand extends Command {

    public MoveCommand(ClientThread clientThread, GameServer server, GameManager gameManager) {
        super(clientThread, server, gameManager);
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 3) {
            return "Usage: " + getUsage();
        }

        String clientId = clientThread.getClientId();
        String gameId;
        int row;
        int col;

        try {
            gameId = clientThread.getCurrentGameId();
            if (gameId == null) {
                return "You are not currently in any game. Please join a game first.";
            }

            row = InputParser.parseRowOrColumn(args[1]);
            col = InputParser.parseRowOrColumn(args[2]);

            HexGame game = gameManager.getGame(gameId);
            if (game == null) {
                return "Game not found";
            }

            game.updateTimeRemaining();
            if (game.isGameEnded()) {
                return handleGameAlreadyEnded(game);
            }

            boolean moveSuccessful = gameManager.makeMove(gameId, clientId, row, col);
            if (moveSuccessful) {
                return handleSuccessfulMove(game, clientId);
            } else {
                return handleFailedMove(game, clientId);
            }

        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String handleSuccessfulMove(HexGame game, String clientId) {
        if (game.isGameEnded() && game.getWinner() != null) {
            String winMessage = createWinMessage(game);
            notifyOpponent(game, clientId, winMessage);

            if (game.getWinner().equals(clientId)) {
                return "Congratulations! You have won the game!\n" + game.getBoardState();
            } else {
                return "Game Over! Your opponent has won the game.\n" + game.getBoardState();
            }
        } else {
            notifyOpponentOfMove(game, clientId);
            return "Move accepted:\n" + game.getPlayerBoardState(clientId);
        }
    }

    private String handleFailedMove(HexGame game, String clientId) {
        if (game.isGameEnded() && game.getWinner() != null) {
            return createTimeoutMessage(game) + "\n" + game.getBoardState();
        }
        return "Invalid move:\n" + game.getPlayerBoardState(clientId);
    }

    private String handleGameAlreadyEnded(HexGame game) {
        String timeoutMsg = "Cannot make a move. Game has ended. ";
        if (game.getWinner() != null) {
            timeoutMsg += "Player " +
                    (game.getWinner().equals(game.getPlayer1Id()) ? "1" : "2") +
                    " (" + (game.getWinner().equals(game.getPlayer1Id()) ? "X" : "O") + ") has won!";
        }
        return timeoutMsg + "\n" + game.getBoardState();
    }

    private String createWinMessage(HexGame game) {
        return "Game Over! Player " +
                (game.getWinner().equals(game.getPlayer1Id()) ? "1 (X)" : "2 (O)") +
                " has won the game!\n" + game.getBoardState();
    }

    private String createTimeoutMessage(HexGame game) {
        return "Time has expired! Player " +
                (game.getWinner().equals(game.getPlayer1Id()) ? "1" : "2") +
                " (" + (game.getWinner().equals(game.getPlayer1Id()) ? "X" : "O") + ") has won!";
    }

    private void notifyOpponent(HexGame game, String clientId, String message) {
        String opponentId = clientId.equals(game.getPlayer1Id()) ?
                game.getPlayer2Id() : game.getPlayer1Id();
        server.notifyClient(opponentId, message);
    }

    private void notifyOpponentOfMove(HexGame game, String clientId) {
        String opponentId = clientId.equals(game.getPlayer1Id()) ?
                game.getPlayer2Id() : game.getPlayer1Id();
        String updatedBoard = game.getPlayerBoardState(opponentId);
        server.notifyClient(opponentId, "Opponent made a move. Your turn now!\n" + updatedBoard);
    }

    @Override
    public String getDescription() {
        return "Make a move (row can be a number, column can be a letter A-Z)";
    }

    @Override
    public String getUsage() {
        return "move <row> <col>";
    }
}
