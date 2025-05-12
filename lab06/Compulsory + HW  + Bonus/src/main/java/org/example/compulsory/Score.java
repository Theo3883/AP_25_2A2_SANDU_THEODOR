package org.example.compulsory;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

@Getter
public class Score extends JPanel {
    private final MainFrame frame;
    private int playerOneScore = 0;
    private int playerTwoScore = 0;

    private final JLabel playerOneLabel;
    private final JLabel playerTwoLabel;

    public Score(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridLayout(2, 1));

        playerOneLabel = new JLabel("Player 1 (Red): 0");
        playerTwoLabel = new JLabel("Player 2 (Blue): 0");

        playerOneLabel.setForeground(Color.RED);
        playerTwoLabel.setForeground(Color.BLUE);

        add(playerOneLabel);
        add(playerTwoLabel);

        setBorder(BorderFactory.createTitledBorder("Score"));
    }

    public void updatePlayerOneScore(int additionalPoints) {
        playerOneScore += additionalPoints;
        playerOneLabel.setText("Player 1 (Red): " + playerOneScore);
    }

    public void updatePlayerTwoScore(int additionalPoints) {
        playerTwoScore += additionalPoints;
        playerTwoLabel.setText("Player 2 (Blue): " + playerTwoScore);
    }

    public void reset() {
        playerOneScore = 0;
        playerTwoScore = 0;
        playerOneLabel.setText("Player 1 (Red): 0");
        playerTwoLabel.setText("Player 2 (Blue): 0");
    }
}