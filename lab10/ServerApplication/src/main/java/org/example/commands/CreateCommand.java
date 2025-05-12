package org.example.commands;

import org.example.server.ClientThread;
import org.example.game.GameManager;
import org.example.server.GameServer;
import org.example.game.HexGame;

public class CreateCommand extends Command {

    public CreateCommand(ClientThread clientThread, GameServer server, GameManager gameManager) {
        super(clientThread, server, gameManager);
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 3) {
            return "Usage: " + getUsage();
        }

        try {
            int boardSize = Integer.parseInt(args[1]);
            long timeControl = Long.parseLong(args[2]);

            if (boardSize < 5 || boardSize > 19) {
                return "Invalid board size. Choose between 5 and 19.";
            }

            if (timeControl < 30 || timeControl > 3600) {
                return "Invalid time control. Choose between 30 and 3600 seconds.";
            }

            HexGame game = gameManager.createGame(boardSize, timeControl);
            game.joinGame(clientThread.getClientId());
            clientThread.setCurrentGameId(game.getGameId());

            return "Game created successfully. Game ID: " + game.getGameId() + "\n" +
                   "You are Player 1 (X). Waiting for another player to join.\n" +
                   game.getGameInfo();
        } catch (NumberFormatException _) {
            return "Invalid number format. Usage: " + getUsage();
        }
    }

    @Override
    public String getDescription() {
        return "Create a new game";
    }

    @Override
    public String getUsage() {
        return "create <board_size> <time_control_seconds>";
    }
}
