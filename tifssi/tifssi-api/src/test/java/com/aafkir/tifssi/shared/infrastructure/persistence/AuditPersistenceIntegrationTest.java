package com.aafkir.tifssi.shared.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.aafkir.tifssi.support.AbstractPostgreSqlIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuditPersistenceIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    @Autowired
    private CompanyRepository companyRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @AfterEach
    void cleanUp() {
        companyRepository.deleteAllInBatch();
    }

    @Test
    void auditFieldsShouldBeManagedOnCreateAndUpdate() throws Exception {
        Company company = new Company();
        company.setLegalName("Acme Conseil");

        Company savedCompany = companyRepository.saveAndFlush(company);
        entityManager.clear();

        Company insertedCompany = companyRepository.findById(savedCompany.getId()).orElseThrow();
        Instant createdAt = insertedCompany.getCreatedAt();
        Instant firstUpdatedAt = insertedCompany.getUpdatedAt();

        assertThat(createdAt).isNotNull();
        assertThat(firstUpdatedAt).isNotNull();
        assertThat(firstUpdatedAt).isEqualTo(createdAt);

        Thread.sleep(10L);
        insertedCompany.setDisplayName("Acme Groupe");
        companyRepository.saveAndFlush(insertedCompany);
        entityManager.clear();

        Company updatedCompany = companyRepository.findById(savedCompany.getId()).orElseThrow();

        assertThat(updatedCompany.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updatedCompany.getUpdatedAt()).isAfter(firstUpdatedAt);
    }
}
