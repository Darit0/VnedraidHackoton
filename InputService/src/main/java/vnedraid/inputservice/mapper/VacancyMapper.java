package vnedraid.inputservice.mapper;

import org.springframework.stereotype.Component;
import vnedraid.inputservice.api.hh.dto.VacancyDto;
import vnedraid.inputservice.models.Vacancy;

import java.time.OffsetDateTime;
import java.util.Map;

@Component
public class VacancyMapper {

    public Vacancy toEntity(VacancyDto d, Map<String, Object> extra) {
        return Vacancy.builder()
                .id(d.getId())
                .name(d.getName())
                .areaName(d.getArea() != null ? d.getArea().getName() : null)
                .employerName(d.getEmployer() != null ? d.getEmployer().getName() : null)
                .publishedAt(parse(d.getPublishedAt()))
                .archived(Boolean.TRUE.equals(d.getArchived()))
                .premium(d.getPremium())
                .hasTest(d.getHasTest())
                .responseLetterRequired(d.getResponseLetterRequired())
                .department(d.getDepartment())
                .area(d.getArea())
                .salary(d.getSalary())
                .salaryRange(d.getSalaryRange())
                .vacancyType(d.getVacancyType())
                .address(d.getAddress())
                .employer(d.getEmployer())
                .snippet(d.getSnippet())
                .schedule(d.getSchedule())
                .experience(d.getExperience())
                .employment(d.getEmployment())
                .employmentForm(d.getEmploymentForm())
                .workFormat(d.getWorkFormat())
                .professionalRoles(d.getProfessionalRoles())
                .relations(d.getRelations())
                .extra(extra)
                .build();
    }

    private OffsetDateTime parse(String iso) {
        return iso == null ? null : OffsetDateTime.parse(iso);
    }
}

