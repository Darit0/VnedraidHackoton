package vnedraid.inputservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "vacancies")
public class Vacanci {

    @Id
    private UUID id;

}
