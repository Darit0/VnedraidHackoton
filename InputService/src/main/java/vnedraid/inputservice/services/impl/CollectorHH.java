package vnedraid.inputservice.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import vnedraid.inputservice.api.hh.config.MonitoringProps;
import vnedraid.inputservice.models.Vacancy;
import vnedraid.inputservice.repo.VacancyRepo;
import vnedraid.inputservice.services.Collector;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectorHH implements Collector {

    private static final int PAGE_SIZE = 50;          // собираем ровно 50 штук
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final WebClient       hhWebClient;
    private final MonitoringProps props;
    private final ObjectMapper    mapper;
    private final VacancyRepo     repo;

    /** вызов раз в минуту (60000 мс по умолчанию) */
    @Override
    @Scheduled(fixedDelayString = "${hh.delay.ms:60000}")
    public void collect() {
        props.getRequests().forEach(this::loadPage0);
    }

    /** тянем только первую страницу (page = 0) по 50 вакансий */
    private void loadPage0(MonitoringProps.Request rq) {
        log.info("▶ HH collect text='{}' area={}", rq.getText(), rq.getArea());

        JsonNode root = hhWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/vacancies")
                        .queryParam("text",         rq.getText())
                        .queryParam("area",         rq.getArea())
                        .queryParam("page",         0)
                        .queryParam("per_page",     PAGE_SIZE)
                        .queryParam("order_by",     "publication_time")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (root == null || !root.has("items") || root.get("items").isEmpty()) return;

        int upserts = 0;
        for (JsonNode item : root.get("items")) {
            try {
                Vacancy v = map(item);
                repo.save(v);
                upserts++;
            } catch (Exception e) {
                log.warn("✖ parse id={} : {}", item.path("id").asText(), e.getMessage());
            }
        }
        log.info("✅ upserted {} records", upserts);
    }

    /** маппинг JSON → Entity (только нужные поля) */
    private Vacancy map(JsonNode n) {
        JsonNode salary = n.path("salary");

        return Vacancy.builder()
                .id(n.path("id").asText())
                .title(n.path("name").asText(null))
                .city(n.path("area").path("name").asText(null))
                .employer(n.path("employer").path("name").asText(null))
                .salaryFrom(getIntOrNull(salary, "from"))
                .salaryTo(getIntOrNull(salary, "to"))
                .currency(salary.isMissingNode() || salary.isNull() ? null : salary.path("currency").asText(null))
                .carRequired(getBooleanOrNull(n, "car_own"))
                .driverLicenseCategories(joinLicenseCategories(n.path("driver_license_types")))
                .schedule(n.path("schedule").path("name").asText(null))
                .publishedAt(parseDate(n.path("published_at").asText(null)))
                .build();
    }

    /* ---------- util ---------- */

    private Integer getIntOrNull(JsonNode node, String field) {
        return node.isMissingNode() || node.isNull() || node.path(field).isMissingNode()
                ? null
                : node.path(field).asInt();
    }

    private Boolean getBooleanOrNull(JsonNode node, String field) {
        return node.isMissingNode() || node.isNull() ? null : node.path(field).asBoolean();
    }

    private String joinLicenseCategories(JsonNode arr) {
        if (!arr.isArray() || arr.isEmpty()) return null;
        StringJoiner sj = new StringJoiner(",");
        arr.forEach(el -> sj.add(el.path("id").asText()));
        return sj.toString();
    }

    private OffsetDateTime parseDate(String iso) {
        return iso == null ? null : OffsetDateTime.parse(iso, ISO);
    }
}
