package com.aafkir.tifssi.projects.infrastructure.repository;

import com.aafkir.tifssi.projects.domain.model.Mission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findAllByProjectIdOrderByIdAsc(Long projectId);
}
