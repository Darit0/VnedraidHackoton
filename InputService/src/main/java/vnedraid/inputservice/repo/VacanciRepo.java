package vnedraid.inputservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import vnedraid.inputservice.models.Vacancy;

import java.util.UUID;

public interface VacanciRepo extends JpaRepository<Vacancy, UUID> {
}
