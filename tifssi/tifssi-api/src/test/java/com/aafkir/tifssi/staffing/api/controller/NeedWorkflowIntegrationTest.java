package com.aafkir.tifssi.staffing.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.domain.model.Contact;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.aafkir.tifssi.crm.infrastructure.repository.ContactRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aafkir.tifssi.projects.infrastructure.repository.ProjectRepository;
import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
import com.aafkir.tifssi.staffing.domain.model.Need;
import com.aafkir.tifssi.staffing.infrastructure.repository.NeedRepository;
import com.aafkir.tifssi.support.AbstractPostgreSqlIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class NeedWorkflowIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private NeedRepository needRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @AfterEach
    void cleanUp() {
        projectRepository.deleteAllInBatch();
        needRepository.deleteAllInBatch();
        contactRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
    }

    @Test
    void winAndCreateProjectShouldCreateDraftProjectAndExposeIt() throws Exception {
        Company company = new Company();
        company.setLegalName("Acme Conseil");
        company = companyRepository.save(company);

        Contact contact = new Contact();
        contact.setCompany(company);
        contact.setFirstName("Lea");
        contact.setLastName("Martin");
        contact = contactRepository.save(contact);

        Need need = new Need();
        need.setCompany(company);
        need.setContact(contact);
        need.setNeedReference("NEED-2026-001");
        need.setTitle("Consultant Java Senior");
        need.setDescription("Build backend Spring Boot.");
        need.setStatus(NeedStatus.OPEN);
        need.setStartDate(java.time.LocalDate.of(2026, 5, 1));
        need.setEndDate(java.time.LocalDate.of(2026, 12, 31));
        need = needRepository.save(need);

        MvcResult mvcResult = mockMvc.perform(post("/api/needs/{needId}/win-and-create-project", need.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").value(company.getId()))
                .andExpect(jsonPath("$.originNeedId").value(need.getId()))
                .andExpect(jsonPath("$.contactId").value(contact.getId()))
                .andExpect(jsonPath("$.projectCode").value("NEED-2026-001"))
                .andExpect(jsonPath("$.projectName").value("Consultant Java Senior"))
                .andExpect(jsonPath("$.description").value("Build backend Spring Boot."))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.startDate").value("2026-05-01"))
                .andExpect(jsonPath("$.endDate").value("2026-12-31"))
                .andReturn();

        long projectId = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).path("id").asLong();

        mockMvc.perform(get("/api/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactId").value(contact.getId()))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        org.assertj.core.api.Assertions.assertThat(mvcResult.getResponse().getHeader("Location"))
                .isEqualTo("http://localhost/api/projects/" + projectId);
    }

    @Test
    void winAndCreateProjectShouldReturnConflictWhenNeedStateIsInvalid() throws Exception {
        Company company = new Company();
        company.setLegalName("Acme Conseil");
        company = companyRepository.save(company);

        Need need = new Need();
        need.setCompany(company);
        need.setTitle("Need not ready");
        need.setStatus(NeedStatus.DRAFT);
        need = needRepository.save(need);

        mockMvc.perform(post("/api/needs/{needId}/win-and-create-project", need.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("cannot be won")));
    }
}
