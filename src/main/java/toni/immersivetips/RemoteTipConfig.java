package toni.immersivetips;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class RemoteTipConfig {
    String id;
    List<String> tooltipLines;

    String modId;
    String itemName;
    List<String> tags;

    String title;
    String condition;
    String priority;

    public static List<RemoteTipConfig> fetchAndParseJson(String url) {
        try {
            String jsonResponse = fetchJson(url);

            Gson gson = new Gson();
            Type listType = new TypeToken<List<RemoteTipConfig>>() {}.getType();
            return gson.fromJson(jsonResponse, listType);
        } catch (IOException | InterruptedException | JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String fetchJson(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch JSON: HTTP " + response.statusCode());
        }

        return response.body();
    }

}
