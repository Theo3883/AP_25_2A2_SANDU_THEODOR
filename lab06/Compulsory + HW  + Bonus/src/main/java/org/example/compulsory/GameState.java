package org.example.compulsory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@AllArgsConstructor
public class GameState {

    private List<DotInfo> redDots;
    private List<DotInfo> blueDots;
    private List<Line> redLines;
    private List<Line> blueLines;
    private int playerOneScore;
    private int playerTwoScore;
    private int gridSize;
    private boolean isPlayerOneTurn;

    // Required for Jackson deserialization
    public GameState() {}


    // Simple inner class to represent a dot's position and selection state
    @Getter
    @Setter
    public static class DotInfo {
        private int x;
        private int y;
        private boolean selected;

        // Required for Jackson deserialization
        public DotInfo() {}

        public DotInfo(int x, int y, boolean selected) {
            this.x = x;
            this.y = y;
            this.selected = selected;
            System.out.println("Point: " + x + ", " + y);
        }

        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
    }


    public boolean isPlayerOneTurn() { return isPlayerOneTurn; }
    public void setPlayerOneTurn(boolean playerOneTurn) { isPlayerOneTurn = playerOneTurn; }
}