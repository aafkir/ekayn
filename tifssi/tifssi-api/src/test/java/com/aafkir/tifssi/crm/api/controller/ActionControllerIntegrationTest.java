package com.aafkir.tifssi.crm.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aafkir.tifssi.crm.domain.enums.ActionStatus;
import com.aafkir.tifssi.crm.domain.enums.ActionType;
import com.aafkir.tifssi.crm.domain.model.Action;
import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.domain.model.Contact;
import com.aafkir.tifssi.crm.infrastructure.repository.ActionRepository;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.aafkir.tifssi.crm.infrastructure.repository.ContactRepository;
import com.aafkir.tifssi.support.AbstractPostgreSqlIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ActionControllerIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ActionRepository actionRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private ContactRepository contactRepository;

    @AfterEach
    void cleanUp() {
        actionRepository.deleteAllInBatch();
        contactRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
    }

    @Test
    void actionEndpointsShouldSupportCrudAndCompanyFilter() throws Exception {
        Company company = createCompany("Acme Conseil");
        Company otherCompany = createCompany("Globex Industrie");
        Contact contact = createContact(company, "Lea", "Martin");
        createAction(company, contact, "Preparation reunion", ActionType.MEETING, ActionStatus.TODO, LocalDate.of(2026, 6, 8));
        createAction(otherCompany, null, "Suivi prospect", ActionType.EMAIL, ActionStatus.DONE, LocalDate.of(2026, 6, 9));

        MvcResult mvcResult = mockMvc.perform(post("/api/actions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": %d,
                                  "contactId": %d,
                                  "type": "CALL",
                                  "subject": "Relance apres atelier",
                                  "comment": "Confirmer la disponibilite du sponsor.",
                                  "dueDate": "2026-06-12",
                                  "status": "TODO",
                                  "responsibleName": "Nadia Mercier",
                                  "nextStep": "Planifier le prochain point"
                                }
                                """.formatted(company.getId(), contact.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").value(company.getId()))
                .andExpect(jsonPath("$.contactId").value(contact.getId()))
                .andExpect(jsonPath("$.type").value("CALL"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andReturn();

        long actionId = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).path("id").asLong();
        org.assertj.core.api.Assertions.assertThat(mvcResult.getResponse().getHeader("Location"))
                .isEqualTo("http://localhost/api/actions/" + actionId);

        mockMvc.perform(get("/api/actions").param("companyId", company.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/actions")
                        .param("companyId", company.getId().toString())
                        .param("type", "CALL")
                        .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(actionId));

        mockMvc.perform(patch("/api/actions/{id}", actionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DONE",
                                  "comment": "Appel effectue, compte-rendu envoye.",
                                  "nextStep": "Attendre validation budget"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.comment").value("Appel effectue, compte-rendu envoye."))
                .andExpect(jsonPath("$.nextStep").value("Attendre validation budget"));

        mockMvc.perform(delete("/api/actions/{id}", actionId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/actions")
                        .param("companyId", company.getId().toString())
                        .param("type", "CALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createShouldRejectContactFromAnotherCompany() throws Exception {
        Company company = createCompany("Acme Conseil");
        Company otherCompany = createCompany("Globex Industrie");
        Contact otherCompanyContact = createContact(otherCompany, "Karim", "Benali");

        mockMvc.perform(post("/api/actions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": %d,
                                  "contactId": %d,
                                  "type": "CALL",
                                  "subject": "Relance",
                                  "status": "TODO"
                                }
                                """.formatted(company.getId(), otherCompanyContact.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Contact must belong to the same company as the action."));
    }

    private Company createCompany(String legalName) {
        Company company = new Company();
        company.setLegalName(legalName);
        company.setDisplayName(legalName);
        return companyRepository.save(company);
    }

    private Contact createContact(Company company, String firstName, String lastName) {
        Contact contact = new Contact();
        contact.setCompany(company);
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setPrimaryContact(true);
        return contactRepository.save(contact);
    }

    private Action createAction(
            Company company,
            Contact contact,
            String subject,
            ActionType type,
            ActionStatus status,
            LocalDate dueDate
    ) {
        Action action = new Action();
        action.setCompany(company);
        action.setContact(contact);
        action.setType(type);
        action.setSubject(subject);
        action.setComment("Fixture");
        action.setDueDate(dueDate);
        action.setStatus(status);
        action.setResponsibleName("Fixture Owner");
        action.setNextStep("Fixture Next Step");
        return actionRepository.save(action);
    }
}
