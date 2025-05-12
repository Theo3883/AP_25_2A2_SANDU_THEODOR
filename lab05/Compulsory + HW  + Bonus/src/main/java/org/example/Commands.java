package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import exceptions.InvalidCommandArgumentsException;
import org.apache.velocity.*;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.awt.*;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.List;
import java.util.stream.Stream;

class ListCommand implements Command {
    @Override
    public void execute(List<String> args) throws IOException {
        Path dir = args.isEmpty() ?
                Shell.getInstance().getCurrentDirectory() :
                Paths.get(args.get(0));

        if (!Files.exists(dir)) {
            System.out.println("Directory does not exist: " + dir);
            return;
        }

        if (!Files.isDirectory(dir)) {
            System.out.println("Not a directory: " + dir);
            return;
        }

        try (Stream<Path> stream = Files.list(dir)) {
            stream.forEach(path -> {
                String fileInfo = Files.isDirectory(path) ?
                        "[DIR] " + path.getFileName() :
                        "      " + path.getFileName();
                System.out.println(fileInfo);
            });
        }
    }

    @Override
    public String getName() {
        return "ls";
    }

    @Override
    public String getDescription() {
        return "List files in a directory: ls [directory]";
    }
}

class ChangeDirectoryCommand implements Command {
    @Override
    public void execute(List<String> args) throws IOException {
        if (args.isEmpty()) {
            System.out.println("Usage: cd <directory>");
            return;
        }

        Shell shell = Shell.getInstance();
        Path currentDir = shell.getCurrentDirectory();
        Path newDir = currentDir.resolve(args.get(0)).normalize();

        if (Files.exists(newDir) && Files.isDirectory(newDir)) {
            shell.setCurrentDirectory(newDir);
        } else {
            System.out.println("Invalid directory: " + newDir);
        }
    }

    @Override
    public String getName() {
        return "cd";
    }

    @Override
    public String getDescription() {
        return "Change directory: cd <directory>";
    }
}

class ExitCommand implements Command {
    private final Shell shell;

    public ExitCommand(Shell shell) {
        this.shell = shell;
    }

    @Override
    public void execute(List<String> args) {
        System.out.println("Exiting shell...");
        shell.stop();
    }

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getDescription() {
        return "Exit the shell";
    }
}

class AddImageCommand implements Command {
    @Override
    public void execute(List<String> args) throws IOException {
        if (args.size() < 3) {
            System.out.println("Usage: add <name> <time(HH:MM:SS)> <path> [tags]");
            return;
        }

        String name = args.get(0);
        String timeStr = args.get(1);
        String path = args.get(2);

        // Parse the time
        LocalTime time;
        try {
            time = LocalTime.parse(timeStr);
        } catch (Exception e) {
            System.out.println("Invalid time format. Use HH:MM:SS");
            return;
        }

        Path imagePath = Paths.get(path);
        if (!Files.exists(imagePath)) {
            System.out.println("Image file does not exist: " + path);
            return;
        }

        // Process tags if present
        ArrayList<String> tags = new ArrayList<>();
        if (args.size() > 3) {
            for (int i = 3; i < args.size(); i++) {
                tags.add(args.get(i));
            }
        }

        Image image = new Image(name, time, path, tags);
        Shell.getInstance().getRepository().add(image);
        System.out.println("Added image: " + name + " at " + time +
                (tags.isEmpty() ? "" : " with tags: " + String.join(", ", tags)));
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Add an image to repository: add <name> <time(HH:MM:SS)> <path> [tags]";
    }
}

class AddAllCommand implements Command {

    @Override
    public void execute(List<String> args) throws IOException {
        if (args.isEmpty()) {
            System.out.println("Usage: addAll <path>");
            return;
        }
        String path = args.getFirst();
        Repository repo = Shell.getInstance().getRepository();
        repo.addAll(path);
    }

    @Override
    public String getName() {
        return "addAll";
    }

    @Override
    public String getDescription() {
        return "Adds automatically to the repository all images from a directory and its subdirectories.";
    }
}

class RemoveImageCommand implements Command {
    @Override
    public void execute(List<String> args) throws IOException {
        if (args.isEmpty()) {
            System.out.println("Usage: remove <name>");
            return;
        }

        String name = args.get(0);
        boolean removed = Shell.getInstance().getRepository().remove(name);

        if (removed) {
            System.out.println("Image removed: " + name);
        } else {
            System.out.println("Image not found: " + name);
        }
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Remove an image from repository: remove <name>";
    }
}

class UpdateImageCommand implements Command {
    @Override
    public void execute(List<String> args) throws IOException {
        if (args.size() < 3) {
            System.out.println("Usage: update <name> <attribute> <new-value>");
            System.out.println("  Attributes: name, path, tags");
            return;
        }

        String name = args.get(0);
        String attribute = args.get(1).toLowerCase();
        String value = args.get(2);

        boolean updated = false;
        Repository repo = Shell.getInstance().getRepository();

        // Regular attribute update
        updated = repo.update(name, attribute, value);

        if (updated) {
            System.out.println("Image updated: " + name);
        } else {
            System.out.println("Failed to update image: " + name);
        }
    }

    @Override
    public String getName() {
        return "update";
    }

    @Override
    public String getDescription() {
        return "Update image attributes: update <name> <attribute> <new-value>";
    }
}

class ListImagesCommand implements Command {
    @Override
    public void execute(List<String> args) {
        List<Image> images = Shell.getInstance().getRepository().getImages();

        if (images.isEmpty()) {
            System.out.println("No images in repository");
            return;
        }

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        System.out.println("Images in repository:");
        System.out.println("--------------------------");
        for (Image img : images) {
            System.out.println("Name: " + img.name());
            System.out.println("Time: " + img.date().format(timeFormat));
            System.out.println("Path: " + img.path());
            if (!img.tags().isEmpty()) {
                System.out.println("Tags: " + String.join(", ", img.tags()));
            }
            System.out.println("--------------------------");
        }
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "List all images in repository";
    }
}

class HelpCommand implements Command {
    @Override
    public void execute(List<String> args) {
        System.out.println("Available commands:");

        Shell.getInstance().getCommands().values().stream()
                .sorted(Comparator.comparing(Command::getName))
                .forEach(cmd -> System.out.println("- " + cmd.getDescription()));
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Display available commands";
    }
}

class ReportCommand implements Command {



    @Override
    public void execute(List<String> args) throws IOException {
        List<Image> images = Shell.getInstance().getRepository().getImages();

        if (images.isEmpty()) {
            System.out.println("No images in repository to create report");
            return;
        }

        // Initialize Velocity
        VelocityEngine velocityEngine = new VelocityEngine();
        Properties props = new Properties();
        props.setProperty(RuntimeConstants.RESOURCE_LOADERS, "class");
        props.setProperty("resource.loader.class.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init(props);

        // Create a context and add data
        VelocityContext context = new VelocityContext();
        context.put("images", images);
        context.put("reportDate", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Get the template
        Template template = velocityEngine.getTemplate("templates/report-template.vm");

        // Create report directory if it doesn't exist
        Path reportDir = Paths.get(System.getProperty("user.home"), "image-reports");
        if (!Files.exists(reportDir)) {
            Files.createDirectories(reportDir);
        }

        // Render the template to a file
        String reportFileName = "image-report-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) +
                ".html";
        Path reportPath = reportDir.resolve(reportFileName);

        try (Writer writer = Files.newBufferedWriter(reportPath)) {
            template.merge(context, writer);
            System.out.println("Report generated at: " + reportPath);

            // Open the report in the default browser
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(reportPath.toUri());
            }
        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "report";
    }

    @Override
    public String getDescription() {
        return "Generate and open HTML report of repository contents";
    }
}

class SaveCommand implements Command {

    private void jsonParser(String filename, List<Image> images) throws IOException {
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        Path filePath = Paths.get(filename);
        if (images.isEmpty()) {
            System.out.println("No images in repository to save");
            return;
        }

        try {
            ObjectWriter ow = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()).writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(images);

            Files.writeString(filePath, json);
            System.out.println("Repository saved to: " + filePath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error saving repository: " + e.getMessage());
        }
    }

    private void plainTextParser(String filename, List<Image> images) throws IOException {
        if (!filename.endsWith(".txt")) {
            filename += ".txt";
        }

        Path filePath = Paths.get(filename);
        if (images.isEmpty()) {
            System.out.println("No images in repository to save");
            return;
        }

        try (var writer = Files.newBufferedWriter(filePath)) {
            writer.write("# Image Repository Backup\n");
            writer.write("# Created: " + LocalDateTime.now() + "\n");
            writer.write("# Format: NAME|TIME|PATH|TAGS\n\n");

            for (Image img : images) {
                StringBuilder line = new StringBuilder();
                line.append(img.name()).append("|");
                line.append(img.date().format(DateTimeFormatter.ISO_LOCAL_TIME)).append("|");
                line.append(img.path()).append("|");

                // Join tags with commas
                String tags = String.join(",", img.tags());
                line.append(tags);

                writer.write(line.toString());
                writer.newLine();
            }

            System.out.println("Repository saved to: " + filePath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error saving repository: " + e.getMessage());
        }
    }

    private void binaryParser(String filename, List<Image> images) throws IOException {
        if (!filename.endsWith(".bin")) {
            filename += ".bin";
        }

        Path filePath = Paths.get(filename);
        if (images.isEmpty()) {
            System.out.println("No images in repository to save");
            return;
        }

        try (var out = Files.newOutputStream(filePath);
             var objOut = new java.io.ObjectOutputStream(out)) {

            // We can't directly serialize Image records, so convert to serializable format
            var serializableImages = new ArrayList<java.io.Serializable>();

            for (Image img : images) {
                // Create a map representation of each image
                var imageMap = new java.util.HashMap<String, java.io.Serializable>();
                imageMap.put("name", img.name());
                imageMap.put("time", img.date().toString());
                imageMap.put("path", img.path());
                imageMap.put("tags", new ArrayList<>(img.tags()));

                serializableImages.add(imageMap);
            }

            // Write the number of images
            objOut.writeInt(images.size());

            // Write each image
            for (var img : serializableImages) {
                objOut.writeObject(img);
            }

            System.out.println("Repository saved to: " + filePath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error saving repository: " + e.getMessage());
        }
    }

    @Override
    public void execute(List<String> args) throws IOException {
        if (args.size() < 2) {
            System.out.println("Usage: save <filename> <format>");
            return;
        }
        String filename = args.get(0);
        String format = args.get(1).toUpperCase();
        List<Image> images = Shell.getInstance().getRepository().getImages();

        switch (format) {
            case "JSON":
                jsonParser(filename, images);
                break;
            case "PLAIN":
                plainTextParser(filename, images);
                break;
            case "BINARY":
                binaryParser(filename, images);
                break;
            default:
                System.out.println("Unsupported format: " + format + " ! Supported formats: JSON, PLAIN, BINARY");
                break;
        }
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getDescription() {
        return "Save repository to file: save <filename> <format>";
    }
}

class LoadCommand implements Command {

    private List<Image> jsonLoader(String filename) throws IOException {
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        Path filePath = Paths.get(filename);
        if (!Files.exists(filePath)) {
            System.out.println("File not found: " + filePath.toAbsolutePath());
            return List.of();
        }

        try {
            String json = Files.readString(filePath);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            // Use TypeReference to handle generic List<Image>
            List<Image> images = mapper.readValue(json,
                    mapper.getTypeFactory().constructCollectionType(List.class, Image.class));

            return images;
        } catch (Exception e) {
            System.err.println("Error loading JSON repository: " + e.getMessage());
            return List.of();
        }
    }

    private List<Image> plainTextLoader(String filename) throws IOException {
        if (!filename.endsWith(".txt")) {
            filename += ".txt";
        }

        Path filePath = Paths.get(filename);
        if (!Files.exists(filePath)) {
            System.out.println("File not found: " + filePath.toAbsolutePath());
            return List.of();
        }

        List<Image> images = new ArrayList<>();
        try (var lines = Files.lines(filePath)) {
            lines.filter(line -> !line.startsWith("#") && !line.isBlank())
                    .forEach(line -> {
                        String[] parts = line.split("\\|", 4);
                        if (parts.length >= 4) {
                            String name = parts[0];
                            LocalTime time;
                            try {
                                time = LocalTime.parse(parts[1]);
                            } catch (Exception e) {
                                time = LocalTime.now();
                                System.err.println("Error parsing time for image " + name + ", using current time");
                            }
                            String path = parts[2];
                            ArrayList<String> tags = new ArrayList<>();
                            if (!parts[3].isEmpty()) {
                                tags.addAll(List.of(parts[3].split(",")));
                            }
                            images.add(new Image(name, time, path, tags));
                        }
                    });

            return images;
        } catch (Exception e) {
            System.err.println("Error loading text repository: " + e.getMessage());
            return List.of();
        }
    }

    private List<Image> binaryLoader(String filename) throws IOException {
        if (!filename.endsWith(".bin")) {
            filename += ".bin";
        }

        Path filePath = Paths.get(filename);
        if (!Files.exists(filePath)) {
            System.out.println("File not found: " + filePath.toAbsolutePath());
            return List.of();
        }

        List<Image> images = new ArrayList<>();
        try (var in = Files.newInputStream(filePath);
             var objIn = new java.io.ObjectInputStream(in)) {

            // Read the number of images
            int count = objIn.readInt();

            // Read each image
            for (int i = 0; i < count; i++) {
                @SuppressWarnings("unchecked")
                java.util.HashMap<String, Object> imageMap =
                        (java.util.HashMap<String, Object>) objIn.readObject();

                String name = (String) imageMap.get("name");
                LocalTime time;
                try {
                    time = LocalTime.parse((String) imageMap.get("time"));
                } catch (Exception e) {
                    time = LocalTime.now();
                    System.err.println("Error parsing time for image " + name + ", using current time");
                }
                String path = (String) imageMap.get("path");

                @SuppressWarnings("unchecked")
                ArrayList<String> tags = (ArrayList<String>) imageMap.get("tags");
                if (tags == null) {
                    tags = new ArrayList<>();
                }

                images.add(new Image(name, time, path, tags));
            }

            return images;
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading binary repository (class not found): " + e.getMessage());
            return List.of();
        } catch (Exception e) {
            System.err.println("Error loading binary repository: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public void execute(List<String> args) throws IOException {
        if (args.size() < 2) {
            System.out.println("Usage: load <filename> <format>");
            return;
        }

        String filename = args.get(0);
        String format = args.get(1).toUpperCase();
        List<Image> images = null;

        switch (format) {
            case "JSON":
                images = jsonLoader(filename);
                break;
            case "PLAIN":
                images = plainTextLoader(filename);
                break;
            case "BINARY":
                images = binaryLoader(filename);
                break;
            default:
                System.out.println("Unsupported format: " + format + " ! Supported formats: JSON, PLAIN, BINARY");
                return;
        }

        if (images != null && !images.isEmpty()) {
            Repository repo = Shell.getInstance().getRepository();
            repo.clear();
            for (Image img : images) {
                repo.add(img);
            }
            System.out.println("Loaded " + images.size() + " images from " + format + " file");
        } else {
            System.out.println("No images were loaded");
        }
    }

    @Override
    public String getName() {
        return "load";
    }

    @Override
    public String getDescription() {
        return "Load repository from file: load <filename> <format>";
    }
}

class FindGroupsCommand implements Command {
    private List<List<Image>> findMaximalGroups(List<Image> images) {
        int n = images.size();
        if (n == 0) return List.of();

        // Build adjacency matrix where graph[i][j] is true if images i and j share a tag
        boolean[][] graph = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (i == j) {
                    graph[i][j] = true; // Self-connection
                } else {
                    boolean shareTag = !Collections.disjoint(images.get(i).tags(), images.get(j).tags());
                    graph[i][j] = shareTag;
                    graph[j][i] = shareTag;
                }
            }
        }

        // Find all maximal cliques
        List<List<Integer>> allCliques = new ArrayList<>();
        for (int k = n; k >= 2; k--) {
            findAllCliquesOfSize(graph, n, k, allCliques);

            // Filter out non-maximal cliques
            filterNonMaximalCliques(allCliques);

            // If we found some cliques, no need to look for smaller ones
            if (!allCliques.isEmpty()) {
                break;
            }
        }

        // Convert cliques of indices to lists of images
        List<List<Image>> result = new ArrayList<>();
        for (List<Integer> clique : allCliques) {
            List<Image> group = new ArrayList<>();
            for (int idx : clique) {
                group.add(images.get(idx));
            }
            result.add(group);
        }

        // Sort groups by size (largest first)
        result.sort((a, b) -> Integer.compare(b.size(), a.size()));
        return result;
    }

    private void findAllCliquesOfSize(boolean[][] graph, int n, int k, List<List<Integer>> results) {
        List<Integer> currentClique = new ArrayList<>();
        findCliqueHelper(graph, n, k, 0, currentClique, results);
    }

    private void findCliqueHelper(boolean[][] graph, int n, int k, int start,
                                  List<Integer> currentClique, List<List<Integer>> results) {
        if (currentClique.size() == k) {
            results.add(new ArrayList<>(currentClique));
            return;
        }

        for (int i = start; i < n; i++) {
            if (isSafe(graph, currentClique, i)) {
                currentClique.add(i);
                findCliqueHelper(graph, n, k, i + 1, currentClique, results);
                currentClique.remove(currentClique.size() - 1);
            }
        }
    }

    private boolean isSafe(boolean[][] graph, List<Integer> currentClique, int v) {
        for (int u : currentClique) {
            if (!graph[u][v]) {
                return false;
            }
        }
        return true;
    }

    private void filterNonMaximalCliques(List<List<Integer>> cliques) {
        if (cliques.size() <= 1) return;

        for (int i = cliques.size() - 1; i >= 0; i--) {
            Set<Integer> cliqueSet = new HashSet<>(cliques.get(i));
            boolean isMaximal = true;

            for (int j = 0; j < cliques.size(); j++) {
                if (i == j) continue;

                Set<Integer> otherSet = new HashSet<>(cliques.get(j));
                if (otherSet.containsAll(cliqueSet) && otherSet.size() > cliqueSet.size()) {
                    isMaximal = false;
                    break;
                }
            }

            if (!isMaximal) {
                cliques.remove(i);
            }
        }
    }

    @Override
    public void execute(List<String> args) throws InvalidCommandArgumentsException {
        Repository repo = Shell.getInstance().getRepository();
        List<Image> images = repo.getImages();

        if (images.isEmpty()) {
            System.out.println("No images in repository.");
            return;
        }

        List<List<Image>> groups = findMaximalGroups(images);

        if (groups.isEmpty()) {
            System.out.println("No groups with common tags found.");
            return;
        }

        System.out.println("Found " + groups.size() + " maximal groups:");

        for (int i = 0; i < groups.size(); i++) {
            List<Image> group = groups.get(i);
            System.out.println("\nGroup " + (i + 1) + " (" + group.size() + " images):");
            System.out.println("----------------------");

            // Find common tags in this group
            Set<String> commonTags = new HashSet<>(group.get(0).tags());
            for (int j = 1; j < group.size(); j++) {
                commonTags.retainAll(group.get(j).tags());
            }

            // Print common tags
            System.out.println("Common tags: " + String.join(", ", commonTags));

            // Print images in this group
            for (Image img : group) {
                System.out.println("- " + img.name() + " [" + String.join(", ", img.tags()) + "]");
            }
        }
    }

    @Override
    public String getName() {
        return "groups";
    }

    @Override
    public String getDescription() {
        return "Find maximal groups of images with common tags";
    }
}
