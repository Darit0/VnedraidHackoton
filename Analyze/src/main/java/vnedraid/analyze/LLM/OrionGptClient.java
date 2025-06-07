package vnedraid.analyze.LLM;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class OrionGptClient {
    private static final String POST_URL = "https://gpt.orionsoft.ru/api/External/PostNewRequest";
    private static final String GET_URL = "https://gpt.orionsoft.ru/api/External/GetNewResponse";
    private static final String COMPLETE_URL = "https://gpt.orionsoft.ru/api/External/CompleteSession";

    private final int operatingSystemCode;
    private final String apiKey;
    private final String userDomainName;

    public OrionGptClient(int operatingSystemCode, String apiKey, String userDomainName) {
        this.operatingSystemCode = operatingSystemCode;
        this.apiKey = apiKey;
        this.userDomainName = userDomainName;
    }

    // Метод отправки запроса
    public CompletableFuture<String> sendMessage(String dialogId, String message) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String json = String.format("""
            {
              "operatingSystemCode": %d,
              "apiKey": "%s",
              "userDomainName": "%s",
              "dialogIdentifier": "%s",
              "aiModelCode": 1,
              "Message": "%s"
            }
            """, operatingSystemCode, apiKey, userDomainName, dialogId, message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(POST_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    // Метод получения ответа
    public CompletableFuture<String> getResponse(String dialogId) {
        HttpClient client = HttpClient.newHttpClient();

        String json = String.format("""
            {
              "operatingSystemCode": %d,
              "apiKey": "%s",
              "dialogIdentifier": "%s"
            }
            """, operatingSystemCode, apiKey, dialogId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GET_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    // Метод завершения диалога
    public CompletableFuture<String> completeSession(String dialogId) {
        HttpClient client = HttpClient.newHttpClient();

        String json = String.format("""
            {
              "operatingSystemCode": %d,
              "apiKey": "%s",
              "dialogIdentifier": "%s"
            }
            """, operatingSystemCode, apiKey, dialogId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(COMPLETE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}
