package vnedraid.inputservice.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import vnedraid.inputservice.api.HH.HhVacanciesResponse;
import vnedraid.inputservice.models.Vacancy;
import vnedraid.inputservice.repo.VacancyRepo;
import vnedraid.inputservice.services.Collector;

import java.util.ArrayList;

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
            HhVacanciesResponse resp = hhWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/vacancies")
                            .queryParam("text",  rq.getText())
                            .queryParam("area",  rq.getArea())
                            .queryParam("page",  page)
                            .queryParam("per_page", 100)
                            .build())
                    .retrieve()
                    .bodyToMono(HhVacanciesResponse.class)
                    .block();

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
                .salaryFrom(i.getSalary()==null?null:i.getSalary().getFrom())
                .salaryTo(i.getSalary()==null?null:i.getSalary().getTo())
                .salaryCurrency(i.getSalary()==null?null:i.getSalary().getCurrency())
                .experience(i.getExperience().getName())
                .professionalRole(firstRole(i))
                .requiresCar(regex("автомобил", i.getDescription()))
                .gender(regex("(мужчин|женщин|м/ж)", i.getDescription())? "указан":"")
                .age(extractAge(i.getDescription()))
                .description(i.getDescription())
                .publishedAt(LocalDateTime.parse(i.getPublished_at(), DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    private String firstRole(HhVacanciesResponse.Item i){
        return i.getProfessional_roles().isEmpty()? null :
                i.getProfessional_roles().get(0).getName();
    }
    private boolean regex(String pat, String html){
        return html!=null && Pattern.compile(pat, Pattern.CASE_INSENSITIVE).matcher(html).find();
    }
    private String extractAge(String html){
        Matcher m = Pattern.compile("\\b(\\d{2})\\s*лет").matcher(
                Optional.ofNullable(html).orElse(""));
        return m.find()? m.group(1) : "";
    }
}

