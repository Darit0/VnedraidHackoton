package vnedraid.inputservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import vnedraid.inputservice.api.HH.HhVacanciesResponse;
import vnedraid.inputservice.api.HH.config.MonitoringProps;
import vnedraid.inputservice.models.Vacancy;
import vnedraid.inputservice.repo.VacancyRepo;
import vnedraid.inputservice.services.Collector;
import org.springframework.http.HttpStatusCode;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CollectorHH implements Collector {

    private final WebClient hhWebClient;
    private final VacancyRepo vacancyRepo;
    private final MonitoringProps props;

    @Override
    @Scheduled(fixedDelayString = "${hh.delay.ms:180000}")   // каждые 3 мин
    public void collect() {
        props.getRequests().forEach(this::fetchAndSave);
    }

    private void fetchAndSave(MonitoringProps.Request rq) {

        int page = 0;
        int totalPages;

        do {
            /* --------------- 1. «Финальная» копия номера страницы --------------- */
            final int currentPage = page;  // теперь переменная в лямбде effectively-final

            /* --------------- 2. Вызов hh.ru с обработкой ошибок и ретраями ----- */
            HhVacanciesResponse resp = hhWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/vacancies")
                            .queryParam("text",  rq.getText())
                            .queryParam("area",  rq.getArea())
                            .queryParam("page",  currentPage)
                            .queryParam("per_page", 100)
                            .build())
                    .retrieve()
                    /* → если пришёл любой 4xx/5xx — формируем исключение с телом ответа */
                    .onStatus(HttpStatusCode::isError, r ->
                            r.bodyToMono(String.class)
                                    .map(body ->
                                            new IllegalStateException("hh.ru error "
                                                    + r.statusCode() + " ➜ " + body)))  // ← map, НЕ flatMap!
                    .bodyToMono(HhVacanciesResponse.class)
                    /* → повторяем ТОЛЬКО при 5xx, экспоненциально 2 s → 4 s → 8 s */
                    .retryWhen(
                            Retry.backoff(3, Duration.ofSeconds(2))
                                    .filter(ex -> ex instanceof WebClientResponseException wce
                                            && wce.getStatusCode().is5xxServerError()))
                    .block();   // блокируем только в MVP

            totalPages = resp.getPages();
            resp.getItems().stream()
                    .map(this::toEntity)
                    .forEach(vacancyRepo::save);

            page++;
        } while (page < totalPages);
    }


    /** Маппинг DTO → Entity + простой regex-парсер */
    private Vacancy toEntity(HhVacanciesResponse.Item i) {
        return Vacancy.builder()
                .id(i.getId())
                .name(i.getName())
                .city(i.getArea().getName())
                .salaryFrom(i.getSalary()==null ? null : i.getSalary().getFrom())
                .salaryTo(i.getSalary()==null ? null : i.getSalary().getTo())
                .salaryCurrency(i.getSalary()==null ? null : i.getSalary().getCurrency())
                .experience(i.getExperience().getName())
                .professionalRole(firstRole(i))
                .requiresCar(hasCar(i.getDescription()))          // ← новая логика
                .gender(hasGender(i.getDescription()) ? "указан" : "")
                .age(extractAge(i.getDescription()))
                .description(i.getDescription())
                .publishedAt(                                      // ← разбор даты через OffsetDateTime
                        OffsetDateTime.parse(i.getPublished_at())
                                .toLocalDateTime())
                .build();
    }

    private String firstRole(HhVacanciesResponse.Item i){
        return i.getProfessional_roles().isEmpty()? null :
                i.getProfessional_roles().get(0).getName();
    }
    private boolean regex(String pat, String html){
        return html!=null && Pattern.compile(pat, Pattern.CASE_INSENSITIVE).matcher(html).find();
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

