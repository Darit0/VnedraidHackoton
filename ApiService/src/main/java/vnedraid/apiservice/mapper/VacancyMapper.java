package vnedraid.apiservice.mapper;

import org.springframework.stereotype.Component;
import vnedraid.apiservice.dto.VacancyDto;
import vnedraid.inputservice.models.Vacancy;

@Component
public class VacancyMapper {
    public VacancyDto toDto(Vacancy v) {
        return VacancyDto.builder()
                .id(v.getId())
                .title(v.getTitle())
                .city(v.getCity())
                .employer(v.getEmployer())
                .salaryFrom(v.getSalaryFrom())
                .salaryTo(v.getSalaryTo())
                .currency(v.getCurrency())
                .professionalRoles(v.getProfessionalRoles())
                .publishedAt(v.getPublishedAt())
                .build();
    }
}
