package vnedraid.inputservice.models;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import vnedraid.inputservice.api.hh.dto.VacancyDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "vacancy_full")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vacancy {

    /* ========== базовые колонки ========== */
    @Id
    private String id;
    private String name;
    private String areaName;
    private String employerName;
    private OffsetDateTime publishedAt;
    private Boolean archived;

    /* ========== jsonb-колонки ========== */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> department;
    private Boolean premium, hasTest, responseLetterRequired;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private VacancyDto.Area area;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private VacancyDto.Salary salary;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private VacancyDto.SalaryRange salaryRange;

    /**
     * 🔑 новое имя поля и тип
     */
    @Type(JsonType.class)
    @Column(name = "vacancy_type", columnDefinition = "jsonb")
    private VacancyDto.VacancyType vacancyType;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private VacancyDto.Address address;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private VacancyDto.Employer employer;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private VacancyDto.Snippet snippet;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private VacancyDto.Schedule schedule;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private VacancyDto.Experience experience;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private VacancyDto.Employment employment;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private VacancyDto.EmploymentForm employmentForm;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<VacancyDto.Dict> workFormat;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<VacancyDto.Dict> professionalRoles;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<Object> relations;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> extra;
}
