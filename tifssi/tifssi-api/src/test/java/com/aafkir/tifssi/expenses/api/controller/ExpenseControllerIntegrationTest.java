package com.aafkir.tifssi.expenses.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.aafkir.tifssi.expenses.infrastructure.repository.ExpenseRepository;
import com.aafkir.tifssi.projects.domain.enums.MissionStatus;
import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
import com.aafkir.tifssi.projects.domain.model.Mission;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.projects.infrastructure.repository.MissionRepository;
import com.aafkir.tifssi.projects.infrastructure.repository.ProjectRepository;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import com.aafkir.tifssi.support.AbstractPostgreSqlIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class ExpenseControllerIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private MissionRepository missionRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @AfterEach
    void cleanUp() {
        expenseRepository.deleteAllInBatch();
        missionRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
        profileRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
    }

    @Test
    void expenseEndpointsShouldSupportCrudListAndSummaryFlow() throws Exception {
        TestData testData = createMissionAndProfile();

        MvcResult mvcResult = mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "missionId": %d,
                                  "profileId": %d,
                                  "expenseDate": "2026-04-10",
                                  "category": "TRAVEL",
                                  "amount": 125.50,
                                  "currency": "EUR",
                                  "comment": "Taxi airport",
                                  "receiptUrl": "https://cdn.example.com/receipt.pdf",
                                  "status": "DRAFT",
                                  "billable": true
                                }
                                """.formatted(testData.mission().getId(), testData.profile().getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.missionId").value(testData.mission().getId()))
                .andExpect(jsonPath("$.profileId").value(testData.profile().getId()))
                .andExpect(jsonPath("$.amount").value(125.50))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.comment").value("Taxi airport"))
                .andExpect(jsonPath("$.receiptUrl").value("https://cdn.example.com/receipt.pdf"))
                .andExpect(jsonPath("$.billable").value(true))
                .andReturn();

        long expenseId = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).path("id").asLong();
        org.assertj.core.api.Assertions.assertThat(mvcResult.getResponse().getHeader("Location"))
                .isEqualTo("http://localhost/api/expenses/" + expenseId);

        mockMvc.perform(get("/api/expenses").param("missionId", testData.mission().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].profileId").value(testData.profile().getId()));

        mockMvc.perform(get("/api/expenses").param("profileId", testData.profile().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].missionId").value(testData.mission().getId()));

        mockMvc.perform(get("/api/expenses").param("billable", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].billable").value(true));

        mockMvc.perform(patch("/api/expenses/{id}", expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "MEAL",
                                  "amount": 45.00,
                                  "currency": "usd",
                                  "comment": "Updated meal",
                                  "receiptUrl": "https://cdn.example.com/meal.pdf",
                                  "status": "VALIDATED",
                                  "billable": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("MEAL"))
                .andExpect(jsonPath("$.amount").value(45.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.comment").value("Updated meal"))
                .andExpect(jsonPath("$.receiptUrl").value("https://cdn.example.com/meal.pdf"))
                .andExpect(jsonPath("$.status").value("VALIDATED"))
                .andExpect(jsonPath("$.billable").value(false));

        mockMvc.perform(get("/api/expenses/summary")
                        .param("missionId", testData.mission().getId().toString())
                        .param("month", "4")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missionId").value(testData.mission().getId()))
                .andExpect(jsonPath("$.month").value(4))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.totalExpenses").value(1))
                .andExpect(jsonPath("$.billableExpenses").value(0))
                .andExpect(jsonPath("$.nonBillableExpenses").value(1))
                .andExpect(jsonPath("$.totalsByCurrency", hasSize(1)))
                .andExpect(jsonPath("$.totalsByCurrency[0].currency").value("USD"))
                .andExpect(jsonPath("$.totalsByCurrency[0].totalAmount").value(45.00))
                .andExpect(jsonPath("$.totalsByCurrency[0].billableAmount").value(0))
                .andExpect(jsonPath("$.totalsByCurrency[0].nonBillableAmount").value(45.00));

        mockMvc.perform(delete("/api/expenses/{id}", expenseId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/expenses").param("missionId", testData.mission().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createShouldRejectExpenseDateOutsideMissionPeriod() throws Exception {
        TestData testData = createMissionAndProfile();

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "missionId": %d,
                                  "profileId": %d,
                                  "expenseDate": "2026-05-10",
                                  "category": "TRAVEL",
                                  "amount": 15.00,
                                  "currency": "EUR",
                                  "status": "DRAFT",
                                  "billable": true
                                }
                                """.formatted(testData.mission().getId(), testData.profile().getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("expenseDate must be less than or equal to mission endDate."));
    }

    private TestData createMissionAndProfile() {
        Company company = new Company();
        company.setLegalName("Acme Conseil");
        company = companyRepository.save(company);

        Profile profile = new Profile();
        profile.setType(ProfileType.INTERNAL);
        profile.setFirstName("Lea");
        profile.setLastName("Martin");
        profile.setEmailAddress("lea@example.com");
        profile.setActive(true);
        profile = profileRepository.save(profile);

        Project project = new Project();
        project.setCompany(company);
        project.setProjectCode("PRJ-EXP-001");
        project.setProjectName("Expenses MVP");
        project.setStatus(ProjectStatus.ACTIVE);
        project = projectRepository.save(project);

        Mission mission = new Mission();
        mission.setProject(project);
        mission.setProfile(profile);
        mission.setRoleName("Developer");
        mission.setStartDate(java.time.LocalDate.of(2026, 4, 1));
        mission.setEndDate(java.time.LocalDate.of(2026, 4, 30));
        mission.setStatus(MissionStatus.ACTIVE);
        mission = missionRepository.save(mission);

        return new TestData(profile, mission);
    }

    private record TestData(Profile profile, Mission mission) {
    }
}
