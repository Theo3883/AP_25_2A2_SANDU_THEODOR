package org.example.commands;

import org.example.server.ClientThread;
import org.example.game.GameManager;
import org.example.server.GameServer;

public class ListCommand extends Command {

    public ListCommand(ClientThread clientThread, GameServer server, GameManager gameManager) {
        super(clientThread, server, gameManager);
    }

    @Override
    public String execute(String[] args) {
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

    @Override
    public String getDescription() {
        return "List all active games";
    }

    @Override
    public String getUsage() {
        return "list";
    }
}
