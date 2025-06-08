package vnedraid.inputservice.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vnedraid.inputservice.models.Vacancy;

public interface VacancyRepo
        extends JpaRepository<Vacancy, String>,
        JpaSpecificationExecutor<Vacancy> {

    Page<Vacancy> findByTitleContainingIgnoreCaseOrProfessionalRolesContainingIgnoreCase(
            String t, String r, Pageable pg);
}
