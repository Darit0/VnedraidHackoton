package vnedraid.analyze.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO для запроса к LLM: содержит задачу (например, "salary_comparison"), синонимы и список вакансий
 */
public class LlmAnalysisRequest {
    private String task;
    private Map<String, List<String>> synonyms;
    private List<VacancyDto> vacancies;

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public Map<String, List<String>> getSynonyms() { return synonyms; }
    public void setSynonyms(Map<String, List<String>> synonyms) { this.synonyms = synonyms; }

    public List<VacancyDto> getVacancies() { return vacancies; }
    public void setVacancies(List<VacancyDto> vacancies) { this.vacancies = vacancies; }
}
