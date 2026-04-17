package com.aafkir.tifssi.crm.infrastructure.repository;

import com.aafkir.tifssi.crm.domain.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}

