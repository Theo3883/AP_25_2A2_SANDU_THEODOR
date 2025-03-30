package org.example.compulsory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class DrawingPanel extends JPanel {
    private final MainFrame frame;
    private BufferedImage image;
    private Graphics2D offscreen;

    private int canvasWidth = 400, canvasHeight = 400;
    private int dotSize = 10;
    private int gridSize = 10;

    // Store dots coordinates and their properties
    private Map<Point, Boolean> dots = new HashMap<>(); // Point -> isSelected
    private Point selectedDot = null;

    public DrawingPanel(MainFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(canvasWidth, canvasHeight));
        createOffscreenImage();
        generateRandomDots(10); // Generate 10 dots initially
        setupMouseListeners();
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
        clear();
        generateRandomDots(gridSize * 2); // Generate more dots when grid size changes
    }

    private void generateRandomDots(int count) {
        dots.clear();
        Random random = new Random();
        int cellWidth = canvasWidth / gridSize;
        int cellHeight = canvasHeight / gridSize;

        // Generate dots at grid intersections
        for (int i = 0; i < count; i++) {
            int gridX = random.nextInt(gridSize);
            int gridY = random.nextInt(gridSize);

            // Calculate actual pixel coordinates
            int x = gridX * cellWidth + cellWidth / 2;
            int y = gridY * cellHeight + cellHeight / 2;

            // Add to our map
            dots.put(new Point(x, y), false);
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
        dots.clear();
        selectedDot = null;
        repaint();
    }

    private void setupMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point clickPoint = findNearestDot(e.getPoint());
                if (clickPoint != null) {
                    if (selectedDot == null) {
                        // First dot selection
                        selectedDot = clickPoint;
                        dots.put(clickPoint, true); // Mark as selected
                    } else {
                        // Second dot selection - draw line
                        drawLine(selectedDot.x, selectedDot.y, clickPoint.x, clickPoint.y);
                        dots.put(selectedDot, false); // Unmark first dot
                        selectedDot = null; // Reset selection
                    }
                    drawAllDots(); // Redraw all dots
                    repaint();
                }
            }
        };

        addMouseListener(mouseAdapter);
    }

    private Point findNearestDot(Point clickPoint) {
        final int CLICK_THRESHOLD = 15; // Sensitivity in pixels

        // Look for the closest dot within threshold
        return dots.keySet().stream()
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
        dots.forEach((point, isSelected) -> {
            if (isSelected) {
                offscreen.setColor(Color.RED); // Selected dot
            } else {
                offscreen.setColor(Color.BLACK); // Normal dot
            }
            offscreen.fillOval(point.x - dotSize/2, point.y - dotSize/2, dotSize, dotSize);
        });
    }

    private void drawLine(int x1, int y1, int x2, int y2) {
        offscreen.setColor(Color.GREEN);
        offscreen.setStroke(new BasicStroke(2)); // Make line thicker
        offscreen.drawLine(x1, y1, x2, y2);
        offscreen.setStroke(new BasicStroke(1)); // Reset stroke
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
}