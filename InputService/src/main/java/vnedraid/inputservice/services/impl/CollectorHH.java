package vnedraid.inputservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import vnedraid.inputservice.api.HH.HhVacanciesResponse;
import vnedraid.inputservice.api.HH.config.MonitoringProps;
import vnedraid.inputservice.models.Vacancy;
import vnedraid.inputservice.repo.VacancyRepo;
import vnedraid.inputservice.services.Collector;

import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectorHH implements Collector {

    private final WebClient hhWebClient;
    private final VacancyRepo vacancyRepo;
    private final MonitoringProps props;

    @Override
    @Scheduled(fixedDelayString = "${hh.delay.ms:180000}")
    public void collect() {
        log.info("⏰ Collector tick!");
        props.getRequests().forEach(this::fetchAndSave);
    }

    private void fetchAndSave(MonitoringProps.Request rq) {
        log.info("⏰ HH Fetch for text='{}' area={}", rq.getText(), rq.getArea());

        // Получаем ответ как массив байт (так и JSON, и GZIP можно обработать)
        byte[] body = hhWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/vacancies")
                        .queryParam("text", rq.getText())
                        .queryParam("area", rq.getArea())
                        .queryParam("page", 0)
                        .queryParam("per_page", 50)
                        .queryParam("no_magic", true)
                        .build())
                .retrieve()
                .bodyToMono(byte[].class)
                .doOnError(e -> log.warn("⚠ HH fetch failed: {}", e.toString()))
                .block();

        if (body == null || body.length == 0) {
            log.warn("⚠ HH empty response for '{}'", rq.getText());
            return;
        }

        String json;
        try {
            // Проверяем GZIP ли это (первая байта 0x1F == 31)
            if (body[0] == (byte)0x1F) {
                log.info("=== Detected GZIP, decompressing...");
                try (java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(body))) {
                    json = new String(gis.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
            } else {
                json = new String(body, java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.warn("⚠ Decompress error: {}", e.toString());
            return;
        }

        if (json.length() < 20) {
            log.warn("⚠ HH returned too short data: '{}'", json);
            return;
        }

        // Лог для отладки: покажи первые 500 символов ответа (или сколько нужно)
        log.debug("=== HH JSON chunk: {}", json.substring(0, Math.min(json.length(), 500)));

        // Парсим JSON в твой DTO
        HhVacanciesResponse resp;
        try {
            // Используй свой кастомный ObjectMapper если надо
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            resp = mapper.readValue(json, HhVacanciesResponse.class);
        } catch (Exception e) {
            log.warn("⚠ JSON parse error: {}", e.toString());
            return;
        }

        if (resp.getItems() == null) {
            log.warn("⚠ HH returned no items for '{}'", rq.getText());
            return;
        }

        resp.getItems().stream()
                .map(this::toEntity)
                .forEach(vacancyRepo::save);

        log.info("💾 saved {} rows for query='{}' area={}", resp.getItems().size(), rq.getText(), rq.getArea());
    }


    /** Маппинг DTO → Entity + простой regex-парсер */
    private Vacancy toEntity(HhVacanciesResponse.Item i) {
        return Vacancy.builder()
                .id(i.getId())
                .name(i.getName())
                .city(i.getArea().getName())
                .salaryFrom(i.getSalary() == null ? null : i.getSalary().getFrom())
                .salaryTo(i.getSalary() == null ? null : i.getSalary().getTo())
                .salaryCurrency(i.getSalary() == null ? null : i.getSalary().getCurrency())
                .experience(i.getExperience().getName())
                .professionalRole(firstRole(i))
                .requiresCar(hasCar(i.getDescription()))
                .gender(hasGender(i.getDescription()) ? "указан" : "")
                .age(extractAge(i.getDescription()))
                .description(i.getDescription())
                .publishedAt(
                        OffsetDateTime.parse(i.getPublished_at())
                                .toLocalDateTime())
                .build();
    }

    private String firstRole(HhVacanciesResponse.Item i){
        return i.getProfessional_roles().isEmpty()? null :
                i.getProfessional_roles().get(0).getName();
    }

    private static final Pattern CAR_PATTERN    = Pattern.compile("(автомобил[ья]|машин[аы])",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern GENDER_PATTERN = Pattern.compile("\\b(мужчин|женщин|м/ж)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern AGE_PATTERN    = Pattern.compile("\\b(\\d{2})\\s*(год(?:а|у)?|лет)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private boolean hasCar(String html)        { return match(CAR_PATTERN, html); }
    private boolean hasGender(String html)     { return match(GENDER_PATTERN, html); }
    private String  extractAge(String html)    {
        Matcher m = AGE_PATTERN.matcher(nullSafe(html));
        return m.find() ? m.group(1) : "";
    }
    private boolean match(Pattern p, String html){ return p.matcher(nullSafe(html)).find(); }
    private String nullSafe(String s){ return s == null ? "" : s; }
}
