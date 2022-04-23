package tracker.controllers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    String uriString;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    private String apiKey;
    private HttpClient client;
    private HttpRequest request;

    public KVTaskClient(String uriString) {
        //  URI uri = URI.create(uriString);
        this.uriString = uriString;
        client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uriString + "/register"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "text/html")
                .build();

        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, handler);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (response.statusCode() == 200) {
            apiKey = "API_KEY=" + response.body();
        }
        System.out.println(apiKey);
    }

    public void put(String key, String json) {
        request = HttpRequest.newBuilder()
                .POST((HttpRequest.BodyPublishers.ofString(json)))
                .uri(URI.create(uriString + "/save/" + key + "?" + apiKey))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, handler);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (response.statusCode() != 200) {
            System.out.println("Ошибка при сохранении значения ключа" + key + " на сервер");
        }
    }

    public String load(String key) {
        request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uriString + "/load/" + key + "?" + apiKey))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, handler);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            return null;
        }
    }
}
