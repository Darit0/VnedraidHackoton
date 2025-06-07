package vnedraid.inputservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import vnedraid.inputservice.models.Candidate;

import java.util.UUID;

public interface СandidateRepo extends JpaRepository<Candidate, UUID> {
}
