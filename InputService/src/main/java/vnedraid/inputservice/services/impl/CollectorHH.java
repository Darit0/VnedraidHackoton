package vnedraid.inputservice.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import vnedraid.inputservice.api.hh.config.MonitoringProps;
import vnedraid.inputservice.api.hh.dto.VacancyDto;
import vnedraid.inputservice.mapper.VacancyMapper;
import vnedraid.inputservice.models.Vacancy;
import vnedraid.inputservice.repo.VacancyRepo;
import vnedraid.inputservice.services.Collector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Service @RequiredArgsConstructor @Slf4j
public class CollectorHH implements Collector {

    private final WebClient hhWebClient;
    private final MonitoringProps props;
    private final ObjectMapper mapper;
    private final VacancyMapper vacancyMapper;
    private final VacancyRepo repo;

    @Override
    @Scheduled(fixedDelayString = "${hh.delay.ms:300000}")
    public void collect() {
        props.getRequests().forEach(this::loadAllPages);
    }

    private void loadAllPages(MonitoringProps.Request rq) {
        log.info("▶ HH collect '{}', area={}", rq.getText(), rq.getArea());
        int page=0, per=50, saved=0;
        Set<String> exists = new HashSet<>(repo.findAll().stream().map(Vacancy::getId).toList());

        while(true){
            final int currentPage = page;
            JsonNode root = hhWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/vacancies")
                            .queryParam("text", rq.getText())
                            .queryParam("area", rq.getArea())
                            .queryParam("page", currentPage)
                            .queryParam("per_page", per)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if(root==null || !root.has("items") || root.get("items").isEmpty()) break;

            for(JsonNode node : root.get("items")){
                String id = node.get("id").asText();
                if(!exists.add(id)) continue;

                try{
                    VacancyDto dto = mapper.treeToValue(node, VacancyDto.class);

                    Map<String,Object> extra = extractUnknown(node, dto);

                    Vacancy ent = vacancyMapper.toEntity(dto, extra);
                    repo.save(ent);
                    saved++;
                }catch(Exception e){
                    log.warn("✖ parse/save id={} : {}", id, e.toString());
                }
            }

            if(root.get("items").size()<per || page>=root.path("pages").asInt()) break;
            page++;
        }
        log.info("✅ saved {} records (text='{}')", saved, rq.getText());
    }

    /** Доп. утилита: находит поля, которых нет в DTO (чтобы ничего не потерять) */
    private Map<String,Object> extractUnknown(JsonNode source, VacancyDto dto){
        Iterator<String> it = source.fieldNames();
        Map<String,Object> map = mapper.convertValue(dto, Map.class);
        while(it.hasNext()){
            String k = it.next();
            if(!map.containsKey(k)) map.put(k, mapper.convertValue(source.get(k), Object.class));
        }
        return map;
    }
}
