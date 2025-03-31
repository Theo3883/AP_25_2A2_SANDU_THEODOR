package org.example.compulsory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.ArrayList;
import java.util.List;

public class DrawingPanel extends JPanel {

    private final MainFrame frame;
    private BufferedImage image;
    private Graphics2D offscreen;

    private int canvasWidth = 400, canvasHeight = 400;
    private int dotSize = 10;
    private int gridSize = 10;

    // Store dots for two players
    private Map<Point, Boolean> playerOneDots = new HashMap<>(); // Red player
    private Map<Point, Boolean> playerTwoDots = new HashMap<>(); // Blue player
    private List<Line> playerOneLines = new ArrayList<>(); // Red player lines
    private List<Line> playerTwoLines = new ArrayList<>(); // Blue player lines

    private boolean isPlayerOneTurn = true; // Track current player's turn
    private Point selectedDot = null;

    public DrawingPanel(MainFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(canvasWidth, canvasHeight));
        createOffscreenImage();
        generateInitialDots(10); // Generate initial dots for both players
        setupMouseListeners();
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
        clear();
        generateInitialDots(gridSize); // Regenerate dots with new grid size
    }

    private void generateInitialDots(int count) {
        playerOneDots.clear();
        playerTwoDots.clear();
        Random random = new Random();
        int cellWidth = canvasWidth / gridSize;
        int cellHeight = canvasHeight / gridSize;

        // Generate dots for player one (red)
        for (int i = 0; i < count; i++) {
            int gridX = random.nextInt(gridSize);
            int gridY = random.nextInt(gridSize);

            int x = gridX * cellWidth + cellWidth / 2;
            int y = gridY * cellHeight + cellHeight / 2;

            Point p = new Point(x, y);
            playerOneDots.put(p, false);
        }

        // Generate dots for player two (blue)
        for (int i = 0; i < count; i++) {
            int gridX = random.nextInt(gridSize);
            int gridY = random.nextInt(gridSize);

            int x = gridX * cellWidth + cellWidth / 2;
            int y = gridY * cellHeight + cellHeight / 2;

            Point p = new Point(x, y);
            // Avoid overlapping dots
            if (!playerOneDots.containsKey(p)) {
                playerTwoDots.put(p, false);
            } else {
                i--; // Try again if dot position is already taken
            }
        }

        // Draw the dots
        drawAllDots();
    }

    private void createOffscreenImage() {
        image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        offscreen = image.createGraphics();
        offscreen.setColor(Color.WHITE);
        offscreen.fillRect(0, 0, canvasWidth, canvasHeight);
        drawGrid();
    }

    public void clear() {
        createOffscreenImage();
        playerOneDots.clear();
        playerTwoDots.clear();
        playerOneLines.clear();
        playerTwoLines.clear();
        selectedDot = null;
        isPlayerOneTurn = true;
        frame.scorePanel.reset();
        repaint();
    }

    private void updateScore(Point p1, Point p2, boolean isPlayerOne) {
        // Calculate Euclidean distance
        double distance = distanceBetween(p1, p2);
        // Convert to int score (could use different scaling if needed)
        int points = (int) Math.round(distance);

        // Update the appropriate player's score
        if (isPlayerOne) {
            frame.scorePanel.updatePlayerOneScore(points);
        } else {
            frame.scorePanel.updatePlayerTwoScore(points);
        }
    }

    private void setupMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point clickPoint = e.getPoint();
                Point redDot = findNearestDot(clickPoint, playerOneDots);
                Point blueDot = findNearestDot(clickPoint, playerTwoDots);

                // Determine which dot is closer (if both are within threshold)
                Point closestDot = null;
                boolean isRedDot = false;

                if (redDot != null && blueDot != null) {
                    double redDistance = distanceBetween(clickPoint, redDot);
                    double blueDistance = distanceBetween(clickPoint, blueDot);

                    if (redDistance <= blueDistance) {
                        closestDot = redDot;
                        isRedDot = true;
                    } else {
                        closestDot = blueDot;
                        isRedDot = false;
                    }
                } else if (redDot != null) {
                    closestDot = redDot;
                    isRedDot = true;
                } else if (blueDot != null) {
                    closestDot = blueDot;
                    isRedDot = false;
                }

                if (closestDot != null) {
                    if (selectedDot == null) {
                        // First dot selection
                        selectedDot = closestDot;
                        isPlayerOneTurn = isRedDot; // Set current player based on selected dot

                        // Mark selected dot
                        if (isPlayerOneTurn) {
                            playerOneDots.put(selectedDot, true);
                        } else {
                            playerTwoDots.put(selectedDot, true);
                        }
                    } else {
                        // Only connect dots of the same color
                        boolean isSameColor = (isPlayerOneTurn && isRedDot) || (!isPlayerOneTurn && !isRedDot);

                        if (isSameColor) {
                            // Second dot selection - draw line
                            Color lineColor = isPlayerOneTurn ? Color.RED : Color.BLUE;
                            drawLine(selectedDot.x, selectedDot.y, closestDot.x, closestDot.y, lineColor);

                            // Update score
                            updateScore(selectedDot, closestDot, isPlayerOneTurn);

                            // Unmark first dot
                            if (isPlayerOneTurn) {
                                playerOneDots.put(selectedDot, false);
                            } else {
                                playerTwoDots.put(selectedDot, false);
                            }

                            // Switch turns after successful connection
                            isPlayerOneTurn = !isPlayerOneTurn;
                        }

                        // Reset selection regardless of successful connection or not
                        selectedDot = null;
                    }

                    drawAllDots(); // Redraw all dots
                    repaint();
                }
            }
        };

        addMouseListener(mouseAdapter);
    }

    private double distanceBetween(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    private Point findNearestDot(Point clickPoint, Map<Point, Boolean> playerDots) {
        final int CLICK_THRESHOLD = 15; // Sensitivity in pixels

        // Look for the closest dot within threshold
        return playerDots.keySet().stream()
                .filter(p -> Math.abs(p.x - clickPoint.x) <= CLICK_THRESHOLD &&
                        Math.abs(p.y - clickPoint.y) <= CLICK_THRESHOLD)
                .min(Comparator.comparingDouble(p ->
                        Math.pow(p.x - clickPoint.x, 2) + Math.pow(p.y - clickPoint.y, 2)))
                .orElse(null);
    }

    private void drawGrid() {
        Color originalColor = offscreen.getColor();
        offscreen.setColor(Color.LIGHT_GRAY);

        int cellWidth = canvasWidth / gridSize;
        int cellHeight = canvasHeight / gridSize;

        // Draw vertical lines
        for (int i = 1; i < gridSize; i++) {
            int x = i * cellWidth;
            offscreen.drawLine(x, 0, x, canvasHeight);
        }

        // Draw horizontal lines
        for (int i = 1; i < gridSize; i++) {
            int y = i * cellHeight;
            offscreen.drawLine(0, y, canvasWidth, y);
        }

        offscreen.setColor(originalColor);
    }

    private void drawAllDots() {
        // Draw player one dots (red)
        playerOneDots.forEach((point, isSelected) -> {
            offscreen.setColor(isSelected ? Color.RED.brighter() : Color.RED);
            offscreen.fillOval(point.x - dotSize/2, point.y - dotSize/2, dotSize, dotSize);
        });

        // Draw player two dots (blue)
        playerTwoDots.forEach((point, isSelected) -> {
            offscreen.setColor(isSelected ? Color.BLUE.brighter() : Color.BLUE);
            offscreen.fillOval(point.x - dotSize/2, point.y - dotSize/2, dotSize, dotSize);
        });
    }

    private void drawLine(int x1, int y1, int x2, int y2, Color color) {
        offscreen.setColor(color);
        offscreen.setStroke(new BasicStroke(2)); // Make line thicker
        offscreen.drawLine(x1, y1, x2, y2);
        offscreen.setStroke(new BasicStroke(1)); // Reset stroke

        // Store the line in the appropriate list
        if (color.equals(Color.RED)) {
            playerOneLines.add(new Line(x1, y1, x2, y2));
        } else if (color.equals(Color.BLUE)) {
            playerTwoLines.add(new Line(x1, y1, x2, y2));
        }
    }

    @Override
    public void update(Graphics g) {
        // No need for update
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.drawImage(image, 0, 0, this);
    }

    public void saveAsPng(String filePath) {
        try {
            // If no specific file path is provided, open a file chooser
            if (filePath == null || filePath.isEmpty()) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Game as PNG");
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                        "PNG Images", "png"));

                if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                    return; // User canceled
                }

                // Get selected file
                filePath = fileChooser.getSelectedFile().getAbsolutePath();

                // Add .png extension if not present
                if (!filePath.toLowerCase().endsWith(".png")) {
                    filePath += ".png";
                }
            }

            // Write the image to file
            File outputFile = new File(filePath);
            ImageIO.write(image, "png", outputFile);



        } catch (IOException e) {
            System.err.println(e);
        }
    }
    public void saveGame() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Game");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Game Save Files", "json"));

            if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return; // User canceled
            }

            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".json")) {
                filePath += ".json";
            }

            // Convert Maps to Lists of DotInfo objects
            List<GameState.DotInfo> redDots = new ArrayList<>();
            List<GameState.DotInfo> blueDots = new ArrayList<>();

            playerOneDots.forEach((point, selected) ->
                    redDots.add(new GameState.DotInfo(point.x, point.y, selected)));
            playerTwoDots.forEach((point, selected) ->
                    blueDots.add(new GameState.DotInfo(point.x, point.y, selected)));

            // Convert lines to Line objects
            List<Line> redLines = new ArrayList<>();
            List<Line> blueLines = new ArrayList<>();

            for (Line line : playerOneLines) {
                redLines.add(new Line(line.x1, line.y1, line.x2, line.y2));
            }

            for (Line line : playerTwoLines) {
                blueLines.add(new Line(line.x1, line.y1, line.x2, line.y2));
            }

            // Create game state
            GameState state = new GameState(
                    redDots,
                    blueDots,
                    redLines,
                    blueLines,
                    frame.scorePanel.getPlayerOneScore(),
                    frame.scorePanel.getPlayerTwoScore(),
                    gridSize,
                    isPlayerOneTurn
            );

            // Use Jackson to serialize
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(filePath), state);

            JOptionPane.showMessageDialog(this,
                    "Game saved successfully!",
                    "Save Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving game: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void loadGame() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Load Game");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Game Save Files", "json"));

            if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                return; // User canceled
            }

            String filePath = fileChooser.getSelectedFile().getAbsolutePath();

            // Deserialize game state
            ObjectMapper mapper = new ObjectMapper();
            GameState state = mapper.readValue(new File(filePath), GameState.class);

            // Reset current game state
            playerOneDots.clear();
            playerTwoDots.clear();
            playerOneLines.clear();
            playerTwoLines.clear();

            // Convert lists back to maps
            for (GameState.DotInfo dot : state.getRedDots()) {
                playerOneDots.put(new Point(dot.getX(), dot.getY()), dot.isSelected());
            }

            for (GameState.DotInfo dot : state.getBlueDots()) {
                playerTwoDots.put(new Point(dot.getX(), dot.getY()), dot.isSelected());
            }

            // Set other game state
            this.gridSize = state.getGridSize();
            this.isPlayerOneTurn = state.isPlayerOneTurn();

            // Update scores
            frame.scorePanel.reset();
            frame.scorePanel.updatePlayerOneScore(state.getPlayerOneScore());
            frame.scorePanel.updatePlayerTwoScore(state.getPlayerTwoScore());

            // Redraw
            createOffscreenImage();
            drawAllDots();

            // Draw the lines using the existing drawLine method
            if (state.getRedLines() != null) {
                for (Line line : state.getRedLines()) {
                    drawLine(line.getX1(), line.getY1(), line.getX2(), line.getY2(), Color.RED);
                }
            }

            if (state.getBlueLines() != null) {
                for (Line line : state.getBlueLines()) {
                    drawLine(line.getX1(), line.getY1(), line.getX2(), line.getY2(), Color.BLUE);
                }
            }

            repaint();

            JOptionPane.showMessageDialog(this,
                    "Game loaded successfully!",
                    "Load Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading game: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

}