package org.example.commands;

import org.example.server.ClientThread;
import org.example.game.GameManager;
import org.example.server.GameServer;
import org.example.game.HexGame;

public class StateCommand extends Command {

    public StateCommand(ClientThread clientThread, GameServer server, GameManager gameManager) {
        super(clientThread, server, gameManager);
    }

    @Override
    public String execute(String[] args) {
        String gameId;
        String clientId = clientThread.getClientId();

        if (args.length >= 2) {
            gameId = args[1];
        } else {
            gameId = clientThread.getCurrentGameId();
            if (gameId == null) {
                return "You are not currently in any game. Please join a game first.";
            }
        }

        HexGame game = gameManager.getGame(gameId);
        if (game == null) {
            return "Game not found";
        }

        game.updateTimeRemaining();
        if (game.isGameEnded() && !args[0].equalsIgnoreCase("move") && game.getWinner() != null) {
            String winnerMsg = "Game has ended. Player " +
                    (game.getWinner().equals(game.getPlayer1Id()) ? "1" : "2") +
                    " (" + (game.getWinner().equals(game.getPlayer1Id()) ? "X" : "O") + ") has won!";
            return winnerMsg + "\n" + game.getBoardState();
        }


        if (!game.isGameStarted()) {
            return game.getGameInfo();
        }
        return game.getPlayerBoardState(clientId);
    }

    @Override
    public String getDescription() {
        return "Get current game state after joining a game";
    }

    @Override
    public String getUsage() {
        return "state";
    }
}
