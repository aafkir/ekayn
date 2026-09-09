package com.aafkir.tifssi.crm.infrastructure.repository;

import com.aafkir.tifssi.crm.domain.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ActionRepository extends JpaRepository<Action, Long>, JpaSpecificationExecutor<Action> {

    long countByCompanyId(Long companyId);
}
