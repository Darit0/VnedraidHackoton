package vnedraid.analyze.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import vnedraid.analyze.dto.LlmAnalysisRequest;

/**
 * Отвечает за отправку данных в LLM (K2 или другую) и получение ответа
 */
@Service
public class LlmClientService {
    private final WebClient webClient;

    public LlmClientService(@Value("${llm.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String analyzeVacancies(LlmAnalysisRequest request) {
        return webClient.post()
                .uri("/analyze")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // блокирующий вызов — если используешь реактивщину, лучше Mono/Flux
    }
}
