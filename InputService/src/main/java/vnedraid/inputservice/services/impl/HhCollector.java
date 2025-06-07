package vnedraid.inputservice.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import vnedraid.inputservice.api.hh.config.MonitoringProps;
import vnedraid.inputservice.api.hh.dto.VacancyDto;
import vnedraid.inputservice.mapper.VacancyMapper;
import vnedraid.inputservice.models.Vacancy;
import vnedraid.inputservice.repo.VacancyRepo;
import vnedraid.inputservice.services.Collector;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Component
public class HhCollector implements Collector {

    private static final int PER_PAGE = 100;
    private static final Duration HH_RATE = Duration.ofSeconds(1);

    private final WebClient                  hh;
    private final MonitoringProps            cfg;
    private final ObjectMapper               om;
    private final VacancyRepo                repo;
    private final VacancyMapper              mapper;
    private final RateLimiter                limiter = RateLimiter.of("hh", RateLimiterConfig.custom()
            .limitRefreshPeriod(HH_RATE).limitForPeriod(1).build());

    @Scheduled(fixedDelayString = "${hh.delay.ms:300000}")
    @Override public void collect() {
        cfg.getRequests().forEach(this::collectForRequest);
    }

    private void collectForRequest(MonitoringProps.Request rq) {
        Flux.range(0, Integer.MAX_VALUE)
                .concatMap(page -> limiter.getPermission(Duration.ofSeconds(5))
                        ? hh.get().uri(uri -> uri.path("/vacancies")
                                .queryParam("text", rq.getText())
                                .queryParam("area", rq.getArea())
                                .queryParam("page", page)
                                .queryParam("per_page", PER_PAGE).build())
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .doOnError(e -> log.warn("HH error page {}: {}", page, e.toString()))
                        .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))))
                .takeUntil(json -> pageFinished(json))
                .flatMap(json -> Flux.fromIterable(json.get("items")))
                .flatMap(this::toVacancy)
                .bufferTimeout(500, Duration.ofSeconds(3))   // batch-upsert
                .filter(list -> !list.isEmpty())
                .flatMap(repo::upsertAll)                    // кастомная репа-операция
                .doOnTerminate(() -> log.info("Finished '{}'", rq.getText()))
                .blockLast();
    }

    private boolean pageFinished(JsonNode root) {
        return !root.has("items") || root.get("items").isEmpty();
    }

    private Mono<Vacancy> toVacancy(JsonNode node) {
        return Mono.fromCallable(() -> {
            VacancyDto dto = om.treeToValue(node, VacancyDto.class);
            return mapper.toEntity(dto, om.convertValue(node, Map.class));
        }).onErrorResume(e -> {
            log.warn("Parse failure id={}", node.path("id").asText());
            return Mono.empty();
        });
    }
}

