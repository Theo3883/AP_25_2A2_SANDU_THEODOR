package org.example.compulsory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

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
        final JButton geminiBtn = new JButton("Gemini");

        private void exitGame(java.awt.event.ActionEvent e) {
            frame.dispose();
            System.exit(0);
        }

        public ControlPanel(MainFrame frame){
            this.frame = frame;
            init();
        }

        private void init() {
            setLayout(new GridLayout(1, 6)); // Update to accommodate 6 buttons
            add(exitBtn);
            add(loadBtn);
            add(saveBtn);
            add(clearBtn);
            add(saveBtnPng);
            add(geminiBtn);

            exitBtn.addActionListener(this::exitGame);
            clearBtn.addActionListener(e -> frame.canvas.clear());
            saveBtnPng.addActionListener(e -> frame.canvas.saveAsPng(null));
            saveBtn.addActionListener(e -> frame.canvas.saveGame());
            loadBtn.addActionListener(e -> frame.canvas.loadGame());
            geminiBtn.addActionListener(e -> askGemini());
        }

        private void askGemini() {


            String prompt = "You are the blue player what is the next move to connect all dots with minimize the distance. Give me only the X and Y of the start and finish for the JavaFx as pixels. Give me only the points, no additional text, or other caracters. use only spaces";

            if (prompt == null || prompt.trim().isEmpty()) {
                return;
            }

            try {

                String tempImagePath = System.getProperty("user.dir") + "/temp_game_image.png";
                frame.canvas.saveAsPng(tempImagePath);


                GeminiService geminiService = new GeminiService();
                String response = geminiService.getGeminiResponseWithImage(prompt, tempImagePath);

                System.out.println(response);
                frame.canvas.processGeminiMove(response);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame,
                        "Error: " + e.getMessage(),
                        "Gemini API Error",
                        JOptionPane.ERROR_MESSAGE);
            }
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

    public static void main(String[] args) throws IOException {
        // Only create one instance, not two
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
        GeminiService geminiService = new GeminiService();

        /*String promt = "You are the blue player what is the next move to connect all dots with minimize the distance. Give me only the X and Y of the start and finish for the JavaFx. Give me only the points, no additional text";
        String imagePath = "testimg.png";


        // Process the file automatically
        String result = geminiService.getGeminiResponseWithImage(promt, imagePath);*/

        //System.out.println(result);
    }
}