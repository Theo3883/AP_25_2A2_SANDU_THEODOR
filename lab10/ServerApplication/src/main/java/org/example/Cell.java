package org.example;

import lombok.Data;

@Data
public class Cell {
    private final int row;
    private final int col;
    private PlayerState owner = PlayerState.EMPTY;
}
