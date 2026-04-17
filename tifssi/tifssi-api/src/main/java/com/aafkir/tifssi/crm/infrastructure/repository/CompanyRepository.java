package com.aafkir.tifssi.crm.infrastructure.repository;

import com.aafkir.tifssi.crm.domain.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}

