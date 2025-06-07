package ru.vnedraid.inputservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vnedraid.analyze.model.Vacancy;

import java.util.List;

public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    List<Vacancy> findByRegionIgnoreCaseAndTitleIgnoreCase(String region, String title);
}
