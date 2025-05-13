package org.example.commands;

import org.example.game.GameManager;
import org.example.game.HexGame;
import org.example.server.ClientThread;
import org.example.server.GameServer;

public class AICommand extends Command {

    public AICommand(ClientThread clientThread, GameServer server, GameManager gameManager) {
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

            String clientId = clientThread.getClientId();

            // Create a game with AI opponent
            HexGame game = gameManager.createAIGame(boardSize, timeControl, clientId);
            clientThread.setCurrentGameId(game.getGameId());

            return "AI game created successfully. Game ID: " + game.getGameId() + "\n"
                    + "You are Player 1 (X). Game is starting!\n"
                    + game.getPlayerBoardState(clientId);

        } catch (NumberFormatException e) {
            return "Invalid number format. Usage: " + getUsage();
        }
    }

    @Override
    public String getDescription() {
        return "Create a new game against AI";
    }

    @Override
    public String getUsage() {
        return "ai <board_size> <time_control_seconds>";
    }
}