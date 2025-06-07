package vnedraid.inputservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import vnedraid.inputservice.models.Vacanci;

import java.util.UUID;

public interface VacanciRepo extends JpaRepository<Vacanci, UUID> {
}
