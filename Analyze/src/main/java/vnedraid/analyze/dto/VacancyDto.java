package vnedraid.analyze.dto;

/**
 * DTO для передачи вакансии в LLM. Расширяй по необходимости!
 */
public class VacancyDto {
    private String title;
    private String region;
    private Integer salaryMin;
    private Integer salaryMax;
    private String company;
    private String skills;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Integer getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Integer salaryMin) { this.salaryMin = salaryMin; }

    public Integer getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Integer salaryMax) { this.salaryMax = salaryMax; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
}
