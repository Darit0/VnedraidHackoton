package vnedraid.analyze.mapper;


import vnedraid.analyze.dto.VacancyDto;
import vnedraid.analyze.model.Vacancy;

/**
 * Утилита для конвертации Vacancy (Entity) в VacancyDto
 */
public class VacancyMapper {
    public static VacancyDto toDto(Vacancy v) {
        VacancyDto dto = new VacancyDto();
        dto.setTitle(v.getName());
        dto.setRegion(v.getCity());
        dto.setSalaryMin(v.getSalaryFrom());
        dto.setSalaryMax(v.getSalaryTo());
        dto.setCompany(v.getEmployer());
        return dto;
    }
}
