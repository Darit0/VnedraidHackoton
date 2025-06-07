package vnedraid.inputservice.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "vacancies")
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
    private String experience; private String education;
    private String professionalRole;
    private LocalDateTime publishedAt;
    @Lob
    private String description;
}
