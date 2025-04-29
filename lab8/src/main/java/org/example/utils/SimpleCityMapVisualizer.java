package org.example.utils;

import org.example.dao.CityDAO;
import org.example.dao.SisterCityDAO;
import org.example.model.City;
import org.example.model.SisterCity;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class SimpleCityMapVisualizer {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final int MARGIN = 50;

    public static void createMap(Connection connection, List<List<City>> biconnectedComponents, String outputFile) {
        try {

            CityDAO cityDAO = new CityDAO(connection);
            SisterCityDAO sisterCityDAO = new SisterCityDAO(connection);

            List<City> cities = cityDAO.findAll();
            List<SisterCity> sisterCities = sisterCityDAO.findAll();

            createMapFromData(cities, sisterCities, biconnectedComponents, outputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createMapFromData(List<City> cities, List<SisterCity> sisterCities,
                                          List<List<City>> biconnectedComponents, String outputFile) {
        try {
            // Find the geographical bounds
            double minLat = 90, maxLat = -90, minLon = 180, maxLon = -180;
            for (City city : cities) {
                minLat = Math.min(minLat, city.getLatitude());
                maxLat = Math.max(maxLat, city.getLatitude());
                minLon = Math.min(minLon, city.getLongitude());
                maxLon = Math.max(maxLon, city.getLongitude());
            }


            Map<Integer, Point> cityPositions = new HashMap<>();


            Map<Integer, Color> componentColors = new HashMap<>();
            Random random = new Random(42);

            for (List<City> biconnectedComponent : biconnectedComponents) {
                Color color = new Color(
                        random.nextInt(256),
                        random.nextInt(256),
                        random.nextInt(256)
                );

                for (City city : biconnectedComponent) {
                    componentColors.put(city.getId(), color);
                }
            }


            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();


            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);


            double xScale = (WIDTH - 2 * MARGIN) / (maxLon - minLon);
            double yScale = (HEIGHT - 2 * MARGIN) / (maxLat - minLat);

            for (City city : cities) {
                int x = (int)((city.getLongitude() - minLon) * xScale) + MARGIN;
                int y = (int)((maxLat - city.getLatitude()) * yScale) + MARGIN;
                cityPositions.put(city.getId(), new Point(x, y));
            }

            for (SisterCity relation : sisterCities) {
                Point p1 = cityPositions.get(relation.getCity1Id());
                Point p2 = cityPositions.get(relation.getCity2Id());

                if (p1 != null && p2 != null) {
                    Color c1 = componentColors.get(relation.getCity1Id());
                    Color c2 = componentColors.get(relation.getCity2Id());

                    if (c1 != null && c2 != null && c1.equals(c2)) {
                        // Same biconnected component
                        g2d.setColor(c1);
                    } else {
                        // Regular connection
                        g2d.setColor(Color.LIGHT_GRAY);
                    }

                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }

            // Draw cities
            for (City city : cities) {
                Point p = cityPositions.get(city.getId());
                if (p != null) {
                    int size = city.isCapital() ? 6 : 3;


                    g2d.setColor(componentColors.getOrDefault(city.getId(), Color.GRAY));
                    g2d.fillOval(p.x - size/2, p.y - size/2, size, size);


                    g2d.setColor(Color.BLACK);
                    g2d.drawOval(p.x - size/2, p.y - size/2, size, size);

                    if (city.isCapital()) {
                        g2d.drawString(city.getName(), p.x + size, p.y);
                    }
                }
            }

            // Draw simple legend
            g2d.setColor(Color.BLACK);
            g2d.drawString("City Network Map - " + biconnectedComponents.size() + " Biconnected Components", 20, 20);

            // Clean up
            g2d.dispose();

            File file = new File(outputFile);
            ImageIO.write(image, "PNG", file);

            System.out.println("Map created and saved to " + file.getAbsolutePath());

            JFrame frame = new JFrame("City Network Map");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new JLabel(new ImageIcon(image)));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}