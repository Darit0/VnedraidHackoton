package vnedraid.apiservice.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacancyDto {
    private String id;
    private String title;
    private String city;
    private String employer;
    private Integer salaryFrom;
    private Integer salaryTo;
    private String currency;
    private String professionalRoles;
    private OffsetDateTime publishedAt;
}
