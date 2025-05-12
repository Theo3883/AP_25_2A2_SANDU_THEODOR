package org.example.player;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlayerState {
    EMPTY(0),
    PLAYER1(1),
    PLAYER2(2);
    private final int value;

    public static PlayerState fromValue(int value) {
        return switch (value) {
            case 1 -> PLAYER1;
            case 2 -> PLAYER2;
            default -> EMPTY;
        };
    }
}
