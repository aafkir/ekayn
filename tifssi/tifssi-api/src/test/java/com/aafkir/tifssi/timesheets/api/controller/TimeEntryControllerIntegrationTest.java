package com.aafkir.tifssi.timesheets.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.aafkir.tifssi.timesheets.infrastructure.repository.TimeEntryRepository;
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
class TimeEntryControllerIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TimeEntryRepository timeEntryRepository;
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
        timeEntryRepository.deleteAllInBatch();
        missionRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
        profileRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
    }

    @Test
    void timeEntryEndpointsShouldSupportCrudListAndSummaryFlow() throws Exception {
        TestData testData = createMissionAndProfile();

        MvcResult mvcResult = mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "missionId": %d,
                                  "profileId": %d,
                                  "workDate": "2026-04-10",
                                  "quantity": 1.00,
                                  "unitType": "DAY",
                                  "comment": "Sprint delivery",
                                  "status": "DRAFT"
                                }
                                """.formatted(testData.mission().getId(), testData.profile().getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.missionId").value(testData.mission().getId()))
                .andExpect(jsonPath("$.profileId").value(testData.profile().getId()))
                .andExpect(jsonPath("$.quantity").value(1.00))
                .andExpect(jsonPath("$.unitType").value("DAY"))
                .andExpect(jsonPath("$.comment").value("Sprint delivery"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        long timeEntryId = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).path("id").asLong();
        org.assertj.core.api.Assertions.assertThat(mvcResult.getResponse().getHeader("Location"))
                .isEqualTo("http://localhost/api/time-entries/" + timeEntryId);

        mockMvc.perform(get("/api/time-entries").param("missionId", testData.mission().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].profileId").value(testData.profile().getId()));

        mockMvc.perform(get("/api/time-entries").param("profileId", testData.profile().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].missionId").value(testData.mission().getId()));

        mockMvc.perform(patch("/api/time-entries/{id}", timeEntryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 0.50,
                                  "unitType": "HALF_DAY",
                                  "comment": "Updated after review",
                                  "status": "VALIDATED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(0.50))
                .andExpect(jsonPath("$.unitType").value("HALF_DAY"))
                .andExpect(jsonPath("$.comment").value("Updated after review"))
                .andExpect(jsonPath("$.status").value("VALIDATED"));

        mockMvc.perform(get("/api/time-entries/summary")
                        .param("missionId", testData.mission().getId().toString())
                        .param("month", "4")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missionId").value(testData.mission().getId()))
                .andExpect(jsonPath("$.month").value(4))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.totalEntries").value(1))
                .andExpect(jsonPath("$.totalDayQuantity").value(0))
                .andExpect(jsonPath("$.totalHalfDayQuantity").value(0.50))
                .andExpect(jsonPath("$.totalHourQuantity").value(0));

        mockMvc.perform(delete("/api/time-entries/{id}", timeEntryId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/time-entries").param("missionId", testData.mission().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createShouldRejectWorkDateOutsideMissionPeriod() throws Exception {
        TestData testData = createMissionAndProfile();

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "missionId": %d,
                                  "profileId": %d,
                                  "workDate": "2026-05-10",
                                  "quantity": 8.00,
                                  "unitType": "HOUR",
                                  "status": "DRAFT"
                                }
                                """.formatted(testData.mission().getId(), testData.profile().getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("workDate must be less than or equal to mission endDate."));
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
        project.setProjectCode("PRJ-TS-001");
        project.setProjectName("Timesheets MVP");
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
