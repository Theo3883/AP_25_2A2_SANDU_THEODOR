package org.example.commands;

import org.example.server.ClientThread;
import org.example.game.GameManager;
import org.example.server.GameServer;
import org.example.game.HexGame;

public class JoinCommand extends Command {

    public JoinCommand(ClientThread clientThread, GameServer server, GameManager gameManager) {
        super(clientThread, server, gameManager);
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 2) {
            return "Usage: " + getUsage();
        }

        String gameId = args[1];
        String clientId = clientThread.getClientId();
        
        if (gameManager.joinGame(gameId, clientId)) {
            HexGame game = gameManager.getGame(gameId);
            String creatorId = game.getPlayer1Id();
            clientThread.setCurrentGameId(gameId);

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

    @Override
    public String getDescription() {
        return "Join an existing game";
    }

    @Override
    public String getUsage() {
        return "join <game_id>";
    }
}
