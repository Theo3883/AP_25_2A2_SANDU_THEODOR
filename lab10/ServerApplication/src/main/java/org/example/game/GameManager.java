package org.example.game;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.AIPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
@Slf4j
public class GameManager {
    private final Map<String, HexGame> activeGames = new ConcurrentHashMap<>();
    private final Map<String, AIPlayer> aiPlayers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService aiExecutor = Executors.newSingleThreadScheduledExecutor();

    public HexGame createGame(int boardSize, long timeControlSeconds) {
        HexGame game = new HexGame(boardSize, timeControlSeconds);

        while (activeGames.containsKey(game.getGameId())) {
            game = new HexGame(boardSize, timeControlSeconds);
        }

        activeGames.put(game.getGameId(), game);
        log.info("New game created: {}", game.getGameId());
        return game;
    }
    
    public HexGame createAIGame(int boardSize, long timeControlSeconds, String humanPlayerId) {
        HexGame game = createGame(boardSize, timeControlSeconds);
        
        // Create AI player with unique ID
        String aiPlayerId = "AI-" + UUID.randomUUID().toString();
        AIPlayer aiPlayer = new AIPlayer(aiPlayerId);
        
        // Store AI player reference
        aiPlayers.put(aiPlayerId, aiPlayer);
        
        // Join game with human player
        game.joinGame(humanPlayerId);
        
        // Join game with AI player
        game.joinGame(aiPlayerId);
        game.setAiGame(true);
        
        // Schedule AI moves if human is player 1
        scheduleAiMove(game);
        
        log.info("New AI game created: {}. Human player: {}, AI player: {}", 
                game.getGameId(), humanPlayerId, aiPlayerId);
        
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
            } else if (game.isAiGame()) {
                scheduleAiMove(game);
            }
        }
        return result;
    }
    
    private void scheduleAiMove(HexGame game) {
        if (game.isGameEnded() || !game.isGameStarted()) {
            return;
        }
        
        String aiPlayerId = game.getPlayer1Id().startsWith("AI-") ? 
                game.getPlayer1Id() : game.getPlayer2Id();
        
        if (!aiPlayerId.startsWith("AI-")) {
            return;
        }
        
        // Check if it's AI's turn
        boolean isAiTurn = (game.getCurrentPlayer() == game.getPlayer1State() && game.getPlayer1Id().equals(aiPlayerId)) ||
                          (game.getCurrentPlayer() == game.getPlayer2State() && game.getPlayer2Id().equals(aiPlayerId));
        
        if (!isAiTurn) {
            return;
        }
        
        // Add a small delay before AI moves to simulate thinking
        aiExecutor.schedule(() -> {
            try {
                if (game.isGameEnded()) {
                    return;
                }
                
                AIPlayer aiPlayer = aiPlayers.get(aiPlayerId);
                if (aiPlayer != null) {
                    int[] move = aiPlayer.makeMove(game);
                    if (move != null) {
                        makeMove(game.getGameId(), aiPlayerId, move[0], move[1]);
                    }
                }
            } catch (Exception e) {
                log.error("Error in AI move execution", e);
            }
        }, 1, TimeUnit.SECONDS);
    }

    public void removeGame(String gameId) {
        HexGame game = activeGames.remove(gameId);
        
        // Clean up AI player if it was an AI game
        if (game != null && game.isAiGame()) {
            if (game.getPlayer1Id().startsWith("AI-")) {
                aiPlayers.remove(game.getPlayer1Id());
            } else if (game.getPlayer2Id().startsWith("AI-")) {
                aiPlayers.remove(game.getPlayer2Id());
            }
        }
        
        log.info("Game removed: {}", gameId);
    }
    
    public void shutdown() {
        aiExecutor.shutdown();
        try {
            if (!aiExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                aiExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            aiExecutor.shutdownNow();
        }
    }
}