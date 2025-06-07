package vnedraid.analyze.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vnedraid.analyze.dto.LlmAnalysisRequest;
import vnedraid.analyze.dto.VacancyDto;
import vnedraid.analyze.mapper.VacancyMapper;
import ru.vnedraid.inputservice.repository.VacancyRepository;
import vnedraid.analyze.model.Vacancy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Основная бизнес-логика: выгружает вакансии, маппит в DTO, отправляет в LLM
 */
@Service
public class AnalyzeService {

    private final VacancyRepository vacancyRepository;
    private final LlmClientService llmClientService;

    @Autowired
    public AnalyzeService(VacancyRepository vacancyRepository, LlmClientService llmClientService) {
        this.vacancyRepository = vacancyRepository;
        this.llmClientService = llmClientService;
    }

    public String analyze(String region, String position, Map<String, List<String>> synonyms) {
        // Фильтруем вакансии (оптимизируй под свою структуру)
        List<Vacancy> vacancies = vacancyRepository
                .findByRegionIgnoreCaseAndTitleIgnoreCase(region, position);

        // Преобразуем в VacancyDto
        List<VacancyDto> vacancyDtos = vacancies.stream()
                .map(VacancyMapper::toDto)
                .collect(Collectors.toList());

        // Собираем запрос для LLM
        LlmAnalysisRequest request = new LlmAnalysisRequest();
        request.setTask("salary_comparison"); // или другое по задаче
        request.setSynonyms(synonyms);
        request.setVacancies(vacancyDtos);

        // Отправляем в LLM и возвращаем ответ
        return llmClientService.analyzeVacancies(request);
    }
}
