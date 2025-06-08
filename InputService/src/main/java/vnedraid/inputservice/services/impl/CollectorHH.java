package vnedraid.inputservice.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectorHH implements Collector {

    /* ---------- константы ---------- */

    private static final int  PAGE_SIZE        = 50;
    private static final int  MAX_PAGES_LOOKUP = 100;

    /** Принимает +03:00, +0300, +03, Z */
    private static final DateTimeFormatter HH_DT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart().appendPattern("XXX").optionalEnd()
            .optionalStart().appendPattern("XX").optionalEnd()
            .optionalStart().appendPattern("X").optionalEnd()
            .optionalStart().appendLiteral('Z').optionalEnd()
            .toFormatter();

    /* ---------- DI ---------- */

    private final WebClient       hhWebClient;
    private final MonitoringProps props;
    private final VacancyRepo     repo;

    /* ---------- state ---------- */

    private final Map<String, Integer> nextPage = new ConcurrentHashMap<>();

    /* ---------- public ---------- */

    @Override
    @Scheduled(fixedDelayString = "${hh.delay.ms:60000}")
    public void collect() {
        props.getRequests().forEach(this::loadNewPortion);
    }

    /* ---------- private ---------- */

    private void loadNewPortion(MonitoringProps.Request rq) {

        final String key = rq.getText() + "|" + rq.getArea();
        int page        = nextPage.getOrDefault(key, 0);
        int inserted    = 0;
        int pagesTried  = 0;
        int totalPages  = Integer.MAX_VALUE;

        log.info("▶ HH collect '{}', area={} — start page={}", rq.getText(), rq.getArea(), page);

        while (inserted < PAGE_SIZE && pagesTried < MAX_PAGES_LOOKUP && page < totalPages) {
            int currentPage = page;
            JsonNode root = hhWebClient.get()
                    .uri(u -> u.path("/vacancies")
                            .queryParam("text",     rq.getText())
                            .queryParam("area",     rq.getArea())
                            .queryParam("page",     currentPage)
                            .queryParam("per_page", PAGE_SIZE)
                            .queryParam("order_by", "publication_time")
                            .queryParam("fields",   "driver_license_types,professional_roles")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (root == null || root.path("items").isEmpty()) break;

            totalPages = root.path("pages").asInt(Integer.MAX_VALUE);

            for (JsonNode item : root.get("items")) {

                String id = item.path("id").asText();
                if (repo.existsById(id)) continue;

                try {
                    repo.save(map(item));
                    inserted++;
                    if (inserted == PAGE_SIZE) break;
                } catch (Exception e) {
                    log.warn("✖ parse id={} : {}", id, e.getMessage());
                }
            }
            page++; pagesTried++;
        }

        nextPage.put(key, (page >= totalPages) ? 0 : page);
        log.info("✅ '{}' area={} — saved {} new vacancies; next start page={}",
                rq.getText(), rq.getArea(), inserted, nextPage.get(key));
    }

    /* ---------- mapping ---------- */

    private Vacancy map(JsonNode n) {
        JsonNode salary = n.path("salary");

        String dlCategories = joinLicenseCategories(n.path("driver_license_types"));
        if (dlCategories == null)
            dlCategories = fetchDlFromVacancy(n.path("id").asText());

        String profRoles    = joinProfessionalRoles(n.path("professional_roles"));

        return Vacancy.builder()
                .id(n.path("id").asText())
                .title(n.path("name").asText(null))
                .city(n.path("area").path("name").asText(null))
                .employer(n.path("employer").path("name").asText(null))
                .salaryFrom(getIntOrNull(salary, "from"))
                .salaryTo(getIntOrNull(salary, "to"))
                .currency(salary.isMissingNode() || salary.isNull()
                        ? null : salary.path("currency").asText(null))
                .carRequired(getBooleanOrNull(n, "car_own"))
                .driverLicenseCategories(dlCategories)
                .professionalRoles(profRoles)                 //  ←  НОВОЕ
                .schedule(n.path("schedule").path("name").asText(null))
                .publishedAt(parseDate(n.path("published_at").asText(null)))
                .build();
    }

    /* ---------- helpers ---------- */

    private String fetchDlFromVacancy(String id) {
        try {
            JsonNode detail = hhWebClient.get()
                    .uri("/vacancies/{id}", id)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            return (detail == null) ? null
                    : joinLicenseCategories(detail.path("driver_license_types"));
        } catch (Exception e) {
            log.debug("⚠ can't load driver_license_types for id={}: {}", id, e.toString());
            return null;
        }
    }

    private Integer getIntOrNull(JsonNode node, String field) {
        return node.isMissingNode() || node.isNull() || node.path(field).isMissingNode()
                ? null : node.path(field).asInt();
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

    /** собираем names из professional_roles */
    private String joinProfessionalRoles(JsonNode arr) {
        if (!arr.isArray() || arr.isEmpty()) return null;
        StringJoiner sj = new StringJoiner(",");
        arr.forEach(el -> sj.add(el.path("name").asText()));
        return sj.toString();
    }

    private OffsetDateTime parseDate(String iso) {
        return (iso == null) ? null : OffsetDateTime.parse(iso, HH_DT);
    }
}
