package org.example.compulsory;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public static class ConfigPanel extends JPanel {
        final MainFrame frame;
        JLabel label;
        JSpinner spinner;

        public ConfigPanel(MainFrame frame) {
            this.frame = frame;
            init();
        }
        private void init() {
            //create the label and the spinner
            label = new JLabel("Number of dots:");
            spinner = new JSpinner(new SpinnerNumberModel(10, 2, 100, 1));

            //create a new game button
            JButton createButton = new JButton("Create New Game");
            createButton.addActionListener(e -> {
                frame.canvas.setGridSize((Integer) spinner.getValue());
            });

            // add components to panel
            add(label);
            add(spinner);
            add(createButton);
        }
    }

    public static class ControlPanel extends JPanel {
        final MainFrame frame;
        final JButton exitBtn = new JButton("Exit");
        final JButton loadBtn = new JButton("Load Game");
        final JButton saveBtn = new JButton("Save Game");
        final JButton clearBtn = new JButton("Clear");
        final JButton saveBtnPng = new JButton("Save as PNG");

        private void exitGame(java.awt.event.ActionEvent e) {
            frame.dispose();
            System.exit(0);
        }

        public ControlPanel(MainFrame frame){
            this.frame = frame;
            init();
        }

        private void init() {
            setLayout(new GridLayout(1, 3));
            add(exitBtn);
            add(loadBtn);
            add(saveBtn);
            add(clearBtn);
            add(saveBtnPng);

            exitBtn.addActionListener(this::exitGame);
            clearBtn.addActionListener(e -> frame.canvas.clear());
            saveBtnPng.addActionListener(e -> frame.canvas.saveAsPng(null));
            saveBtn.addActionListener(e -> frame.canvas.saveGame());
            loadBtn.addActionListener(e -> frame.canvas.loadGame());
        }
    }

    ConfigPanel configPanel;
    ControlPanel controlPanel;
    DrawingPanel canvas;
    Score scorePanel;

    public MainFrame() {
        super("My Drawing Application");
        init();
    }

    private void init() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Set BorderLayout explicitly
        setLayout(new BorderLayout());

        // Create the components
        canvas = new DrawingPanel(this);
        controlPanel = new ControlPanel(this);
        configPanel = new ConfigPanel(this);
        scorePanel = new Score(this);

        //top of the game
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(configPanel, BorderLayout.CENTER);
        topPanel.add(scorePanel, BorderLayout.EAST);

        // Add components with proper BorderLayout constraints
        add(canvas, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Set size and make pack
        setSize(500, 500);
        pack();
    }

    public static void main(String[] args) {
        // Only create one instance, not two
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}