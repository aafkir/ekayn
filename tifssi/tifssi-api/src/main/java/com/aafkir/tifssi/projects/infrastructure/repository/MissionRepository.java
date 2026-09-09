package com.aafkir.tifssi.projects.infrastructure.repository;

import com.aafkir.tifssi.projects.domain.model.Mission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findAllByProjectIdOrderByIdAsc(Long projectId);

    Optional<Mission> findByProjectProjectCodeAndProfileEmailAddressAndRoleName(String projectCode, String profileEmailAddress, String roleName);
}
