package org.example;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Slf4j
public class GameManager {
    private final Map<String, HexGame> activeGames = new ConcurrentHashMap<>();

    public HexGame createGame(int boardSize, long timeControlSeconds) {
        HexGame game = new HexGame(boardSize, timeControlSeconds);

        while (activeGames.containsKey(game.getGameId())) {
            game = new HexGame(boardSize, timeControlSeconds);
        }

        activeGames.put(game.getGameId(), game);
        log.info("New game created: {}", game.getGameId());
        return game;
    }

    public HexGame getGame(String gameId) {
        return activeGames.get(gameId);
    }

    public boolean joinGame(String gameId, String playerId) {
        HexGame game = getGame(gameId);
        if (game == null) {
            return false;
        }
        boolean result = game.joinGame(playerId);
        if (result) {
            log.info("Player {} joined game {}", playerId, gameId);
        }
        return result;
    }

    public boolean makeMove(String gameId, String playerId, int row, int col) {
        HexGame game = getGame(gameId);
        if (game == null) {
            return false;
        }
        boolean result = game.makeMove(playerId, row, col);
        if (result) {
            log.info("Player {} made move ({},{}) in game {}", playerId, row, col, gameId);

            if (game.isGameEnded() && game.getWinner() != null) {
                log.info("Game {} ended with winner: {}", gameId, game.getWinner());
            }
        }
        return result;
    }

    public void removeGame(String gameId) {
        activeGames.remove(gameId);
        log.info("Game removed: {}", gameId);
    }
}