package vnedraid.inputservice.models;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@Table(name = "candidates")
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {
    @Id
    private String id;
    private String fullName;
    private String city;
    private Integer salaryExpectation;
    private String gender;
    private String age;
    private String experience;
    private String education;
    @ElementCollection
    private List<String> skills;
    private String resumeUrl;
    private LocalDateTime fetchedAt;
}
