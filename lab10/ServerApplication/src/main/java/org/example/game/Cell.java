package org.example.game;

import lombok.Data;
import org.example.player.PlayerState;

@Data
public class Cell {
    private final int row;
    private final int col;
    private PlayerState owner = PlayerState.EMPTY;
}
