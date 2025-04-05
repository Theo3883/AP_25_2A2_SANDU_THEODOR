package org.example.compulsory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeminiService {
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";
    private static final String API_KEY = System.getenv("API_KEY");


    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public String getGeminiResponseWithImage(String prompt, String imagePath) {
        String url = GEMINI_API_URL + API_KEY;

        try {
            // Read image file and encode as base64
            byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Determine MIME type based on file extension
            String extension = imagePath.substring(imagePath.lastIndexOf(".") + 1).toLowerCase();
            String mimeType = switch (extension) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "gif" -> "image/gif";
                case "webp" -> "image/webp";
                default -> "application/octet-stream";
            };

            // Prepare request payload
            Map<String, Object> requestMap = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();

            // Add text part
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);
            parts.add(textPart);

            // Add image part
            Map<String, Object> imagePart = new HashMap<>();
            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mimeType", mimeType);
            inlineData.put("data", base64Image);
            imagePart.put("inlineData", inlineData);
            parts.add(imagePart);

            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("parts", parts);
            requestMap.put("contents", List.of(contentMap));

            String requestBody = objectMapper.writeValueAsString(requestMap);

            // Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send request and get response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Process response
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);

                if (responseMap != null && responseMap.containsKey("candidates")) {
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
                    if (!candidates.isEmpty()) {
                        Map<String, Object> firstCandidate = candidates.get(0);
                        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                        List<Map<String, Object>> contentParts = (List<Map<String, Object>>) content.get("parts");

                        if (!contentParts.isEmpty()) {
                            return (String) contentParts.get(0).get("text");
                        }
                    }
                    return "No text content found in response";
                } else {
                    return "Error: No candidates found in response";
                }
            } else {
                return "Error: API returned status code " + response.statusCode() + "\n" + response.body();
            }
        } catch (Exception e) {
            return "Error while contacting Gemini API: " + e.getMessage();
        }
    }

}