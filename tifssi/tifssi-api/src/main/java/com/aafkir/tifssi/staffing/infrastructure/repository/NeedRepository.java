package com.aafkir.tifssi.staffing.infrastructure.repository;

import com.aafkir.tifssi.staffing.domain.model.Need;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NeedRepository extends JpaRepository<Need, Long> {
    long countByCompanyId(Long companyId);
}
