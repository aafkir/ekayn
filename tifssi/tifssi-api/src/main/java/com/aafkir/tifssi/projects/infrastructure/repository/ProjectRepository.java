package com.aafkir.tifssi.projects.infrastructure.repository;

import com.aafkir.tifssi.projects.domain.model.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    long countByCompanyId(Long companyId);

    Optional<Project> findByProjectCode(String projectCode);
}
