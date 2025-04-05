package org.example.compulsory;
/*import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.HarmCategory;
import com.google.cloud.vertexai.api.SafetySetting;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
*/
public class MultiTurnMultimodal {
    /*
    public static void generate(String[] args) throws IOException {
        try (VertexAI vertexAi = new VertexAI("gemeni-api-455609", "global"); ) {
            GenerationConfig generationConfig =
                    GenerationConfig.newBuilder()
                            .setMaxOutputTokens(1024)
                            .setTemperature(0.2F)
                            .setTopP(0.8F)
                            .build();
            List<SafetySetting> safetySettings = Arrays.asList(
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                            //.setThreshold(SafetySetting.HarmBlockThreshold.OFF)
                            .build(),
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                            //.setThreshold(SafetySetting.HarmBlockThreshold.OFF)
                            .build(),
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                            //.setThreshold(SafetySetting.HarmBlockThreshold.OFF)
                            .build(),
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_HARASSMENT)
                            //.setThreshold(SafetySetting.HarmBlockThreshold.OFF)
                            .build()
            );
            GenerativeModel model =
                    new GenerativeModel.Builder()
                            .setModelName("gemini-1.5-flash-002")
                            .setVertexAi(vertexAi)
                            .setGenerationConfig(generationConfig)
                            .setSafetySettings(safetySettings)
                            .build();

            File msg1Image1File = new File("<path of msg1Image1>");
            byte[] msg1Image1Bytes = new byte[(int) msg1Image1File.length()];
            try(FileInputStream msg1Image1FileInputStream = new FileInputStream(msg1Image1File)) {
                msg1Image1FileInputStream.read(msg1Image1Bytes);
            }
            var msg1Image1 = PartMaker.fromMimeTypeAndData(
                    "image/png", msg1Image1Bytes);
            var msg1Text1 = "You are the blue player what is the next move to connect all dots with minimize the distance. Give me only the X and Y of the start and finish. Give me only the points, no additional text";

            // For multi-turn responses, start a chat session.
            ChatSession chatSession = model.startChat();

            GenerateContentResponse response;
            response = chatSession.sendMessage(ContentMaker.fromMultiModalData(msg1Image1, msg1Text1));
            System.out.println(ResponseHandler.getText(response));
        }
    }*/
}