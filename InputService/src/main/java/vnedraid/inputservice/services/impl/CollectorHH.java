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
        // 1. Запрашиваем только первую страницу, 50 вакансий
        HhVacanciesResponse resp = hhWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/vacancies")
                        .queryParam("text", rq.getText())
                        .queryParam("area", rq.getArea())
                        .queryParam("page", 0)
                        .queryParam("per_page", 50)
                        .queryParam("no_magic", "true")
                        .build())
                .retrieve()
                .bodyToMono(HhVacanciesResponse.class)
                .doOnError(e -> log.warn("⚠ hh decode failed: {}", e.toString()))
                .onErrorReturn(new HhVacanciesResponse()) // если парсинг провалился — пустая обёртка
                .block();

        if (resp.getItems() == null) {
            log.warn("⚠ empty items for '{}'", rq.getText());
            return;
        }

        resp.getItems().stream()
                .map(this::toEntity)
                .forEach(vacancyRepo::save);

        log.info("💾 saved {} rows for query='{}' area={}",
                resp.getItems().size(), rq.getText(), rq.getArea());
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
