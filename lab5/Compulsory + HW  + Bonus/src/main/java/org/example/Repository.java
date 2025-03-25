// Repository.java updates
package org.example;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Repository {
    private final ArrayList<Image> images;

    public Repository() {
        this.images = new ArrayList<>();
    }

    public void add(Image image) {
        this.images.add(image);
    }

    public boolean remove(String name) {
        return images.removeIf(img -> img.name().equals(name));
    }

    public boolean update(String name, String attribute, String value) {
        for (int i = 0; i < images.size(); i++) {
            Image img = images.get(i);
            if (img.name().equals(name)) {
                Image updated;
                switch (attribute) {
                    case "name":
                        updated = new Image(value, img.date(), img.path(), img.tags());
                        break;
                    case "path":
                        updated = new Image(img.name(), img.date(), value, img.tags());
                        break;
                    case "tags":
                        ArrayList<String> newTags = new ArrayList<>();
                        if (!value.isEmpty()) {
                            newTags.addAll(List.of(value.split(",")));
                        }
                        updated = new Image(img.name(), img.date(), img.path(), newTags);
                        break;
                    default:
                        return false;
                }
                images.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public void clear() {
        images.clear();
    }

    public List<Image> getImages() {
        return new ArrayList<>(images);
    }

    public void printImages() throws IOException {
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            for (Image image : images) {
                desktop.open(new File(image.path()));
            }
        }
    }

    public void addAll(String path) throws IOException {
        Path directory = Paths.get(path);

        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            System.out.println("Not a valid directory: " + path);
            return;
        }

        // Common image file extensions
        List<String> imageExtensions = List.of(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff");
        int count = 0;

        try (Stream<Path> walk = Files.walk(directory)) {
            List<Path> imageFiles = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String fileName = p.getFileName().toString().toLowerCase();
                        return imageExtensions.stream().anyMatch(fileName::endsWith);
                    })
                    .toList();

            for (Path imageFile : imageFiles) {
                // Extract filename without extension for image name
                String fileName = imageFile.getFileName().toString();
                String name = fileName.lastIndexOf(".") > 0 ?
                        fileName.substring(0, fileName.lastIndexOf(".")) : fileName;

                // Use current time since we don't want to modify the original file
                LocalTime time = LocalTime.now();

                // Create tags from directory structure
                ArrayList<String> tags = new ArrayList<>();

                // Add parent directory as a tag
                String dirName = imageFile.getParent().getFileName().toString();
                tags.add(dirName);

                // Add some random tags based on the image name
                if (name.length() > 3) {
                    tags.add(name.substring(0, 3));
                }

                // Add some random common tags
                List<String> commonTags = List.of("photo", "image", "picture", "media", "collection");
                int randomTagCount = (int) (Math.random() * 3); // 0-2 random tags
                for (int i = 0; i < randomTagCount; i++) {
                    int randomIndex = (int) (Math.random() * commonTags.size());
                    String randomTag = commonTags.get(randomIndex);
                    if (!tags.contains(randomTag)) {
                        tags.add(randomTag);
                    }
                }

                Image image = new Image(name, time, imageFile.toString(), tags);
                this.add(image);
                count++;
            }
        }

        System.out.println("Added " + count + " images from: " + path);
    }
}