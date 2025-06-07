package vnedraid.analyze.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@Table(name = "vacancies")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy {
    @Id
    private String id;
    @Column(nullable=false)
    private String name;
    private String city;
    private Integer salaryFrom;
    private Integer salaryTo;
    private String salaryCurrency;
    private String gender;
    private String age;
    private Boolean requiresCar;
    private String experience;
    private String education;
    private String employer;
    private String professionalRole;
    private LocalDateTime publishedAt;
    @Lob
    private String description;
}
