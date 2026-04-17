package com.aafkir.tifssi.projects.infrastructure.repository;

import com.aafkir.tifssi.projects.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}

