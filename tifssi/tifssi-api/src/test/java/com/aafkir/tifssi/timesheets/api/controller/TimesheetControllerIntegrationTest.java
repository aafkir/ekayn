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
class TimesheetControllerIntegrationTest extends AbstractPostgreSqlIntegrationTest {

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

    @Autowired
    private com.aafkir.tifssi.timesheets.infrastructure.repository.TimesheetRepository timesheetRepository;

    @Autowired
    private com.aafkir.tifssi.absences.infrastructure.repository.AbsenceRepository absences;

    @AfterEach
    void cleanUp() {
        absences.deleteAllInBatch();
        timeEntryRepository.deleteAllInBatch();
        timesheetRepository.deleteAllInBatch();
        missionRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
        profileRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
    }

    @Test
    void shouldGroupByProfileAndMonthAcrossMissionsAndApplyEveryFilter() throws Exception {
        TestData first = createMissionAndProfile();
        TestData other = createMissionAndProfile();
        Mission secondMission = new Mission();
        secondMission.setProfile(first.profile());
        secondMission.setProject(first.mission().getProject());
        secondMission.setRoleName("Second mission");
        secondMission.setStatus(MissionStatus.ACTIVE);
        secondMission = missionRepository.save(secondMission);
        createEntry(first, "2026-04-02");
        createEntry(new TestData(first.profile(), secondMission), "2026-04-01");
        createEntry(first, "2026-05-01");
        createEntry(first, "2025-04-01");
        createEntry(other, "2026-04-01");
        mockMvc.perform(get("/api/timesheets")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(4)));
        mockMvc.perform(get("/api/timesheets").param("profileId", first.profile().getId().toString()).param("year", "2026"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/api/timesheets").param("profileId", first.profile().getId().toString()).param("month", "4"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/api/timesheets").param("month", "4"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)));
        long id = sheetId(first, 2026, 4);
        mockMvc.perform(get("/api/timesheets/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.timeEntries", hasSize(2)))
                .andExpect(jsonPath("$.timeEntries[0].missionId").value(secondMission.getId()))
                .andExpect(jsonPath("$.timeEntries[0].workDate").value("2026-04-01"));
    }

    @Test
    void shouldSubmitRejectCorrectResubmitAndValidateWholeSheetWithoutChangingAbsencesOrEntryStatuses() throws Exception {
        TestData data = createMissionAndProfile();
        long entry = createEntry(data, "2026-04-10");
        createEntry(data, "2026-04-11");
        long id = sheetId(data, 2026, 4);
        mockMvc.perform(post("/api/absences").contentType(MediaType.APPLICATION_JSON).content("""
                {"profileId": %d, "type":"PAID_LEAVE", "startDate":"2026-04-12", "endDate":"2026-04-12", "quantity":1, "status":"APPROVED"}
                """.formatted(data.profile().getId()))).andExpect(status().isCreated());
        mockMvc.perform(get("/api/timesheets/{id}", id)).andExpect(jsonPath("$.status").value("DRAFT"));
        mockMvc.perform(post("/api/timesheets/{id}/validate", id)).andExpect(status().isConflict());
        mockMvc.perform(post("/api/timesheets/{id}/submit", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED")).andExpect(jsonPath("$.submittedAt").isNotEmpty());
        assertEntryLocked(data, entry);
        mockMvc.perform(post("/api/timesheets/{id}/submit", id)).andExpect(status().isConflict());
        mockMvc.perform(post("/api/timesheets/{id}/reject", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("reason", "  Corriger le 10 avril  ", "managerId", 7))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Corriger le 10 avril")).andExpect(jsonPath("$.rejectedBy").value(7));
        mockMvc.perform(patch("/api/time-entries/{id}", entry).contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":0.5}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/timesheets/{id}/submit", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.rejectionReason").isEmpty()).andExpect(jsonPath("$.rejectedAt").isEmpty());
        mockMvc.perform(post("/api/timesheets/{id}/validate", id).param("managerId", "7"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("VALIDATED"))
                .andExpect(jsonPath("$.validatedBy").value(7)).andExpect(jsonPath("$.validatedAt").isNotEmpty())
                .andExpect(jsonPath("$.timeEntries[0].status").value("DRAFT"))
                .andExpect(jsonPath("$.timeEntries[1].status").value("DRAFT"));
        assertEntryLocked(data, entry);
        mockMvc.perform(post("/api/timesheets/{id}/reject", id).contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Too late\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/absences").param("profileId", data.profile().getId().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void shouldDisallowIndividualValidationAndRequireAReasonForMonthlyRejection() throws Exception {
        TestData data = createMissionAndProfile();
        long entry = createEntry(data, "2026-04-10");
        long id = sheetId(data, 2026, 4);
        mockMvc.perform(patch("/api/time-entries/{id}", entry).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"VALIDATED\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/time-entries").contentType(MediaType.APPLICATION_JSON).content("""
                {"profileId":%d,"missionId":%d,"workDate":"2026-04-12","quantity":1,"unitType":"DAY","status":"VALIDATED"}
                """.formatted(data.profile().getId(), data.mission().getId()))).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/timesheets/{id}/submit", id)).andExpect(status().isOk());
        for (String reason : new String[]{"", "  ", "x".repeat(2001)}) {
            mockMvc.perform(post("/api/timesheets/{id}/reject", id).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(java.util.Map.of("reason", reason)))).andExpect(status().isBadRequest());
        }
        mockMvc.perform(post("/api/timesheets/{id}/reject", id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/timesheets/{id}", id)).andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void shouldMoveEditableEntriesBetweenMonthsAndNeverIntoASubmittedSheet() throws Exception {
        TestData data = createMissionAndProfile();
        long entry = createEntry(data, "2026-04-10");
        long oldSheet = sheetId(data, 2026, 4);
        mockMvc.perform(patch("/api/time-entries/{id}", entry).contentType(MediaType.APPLICATION_JSON).content("{\"workDate\":\"2026-05-10\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/timesheets/{id}", oldSheet)).andExpect(jsonPath("$.timeEntries", hasSize(0)));
        mockMvc.perform(post("/api/timesheets/{id}/submit", oldSheet)).andExpect(status().isConflict());
        long newSheet = sheetId(data, 2026, 5);
        mockMvc.perform(get("/api/timesheets/{id}", newSheet)).andExpect(jsonPath("$.timeEntries", hasSize(1)));
        mockMvc.perform(post("/api/timesheets/{id}/submit", newSheet)).andExpect(status().isOk());
        long second = createEntry(data, "2026-04-11");
        mockMvc.perform(patch("/api/time-entries/{id}", second).contentType(MediaType.APPLICATION_JSON).content("{\"workDate\":\"2026-05-11\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/timesheets/{id}", oldSheet)).andExpect(jsonPath("$.timeEntries", hasSize(1)));
    }

    @Test
    void invalidEditsShouldNotMoveEntriesOrCreateAnotherMonthlySheet() throws Exception {
        TestData data = createMissionAndProfile();
        long entry = createEntry(data, "2026-04-10");
        for (String payload : new String[]{"{\"workDate\":null}", "{\"workDate\":\"2026-05-10\",\"quantity\":null}"}) {
            mockMvc.perform(patch("/api/time-entries/{id}", entry).contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(status().isBadRequest());
        }
        mockMvc.perform(get("/api/timesheets")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].timeEntries[0].workDate").value("2026-04-10"));
    }

    @Test
    void shouldValidateFiltersAndUnknownSheets() throws Exception {
        mockMvc.perform(get("/api/timesheets").param("month", "13")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/timesheets").param("month", "0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/timesheets").param("year", "1999")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/timesheets/999999")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/timesheets/999999/submit")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/timesheets/999999/validate")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/timesheets/999999/reject").contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Missing\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void concurrentFirstEntriesShouldCreateOnlyOneMonthlySheet() throws Exception {
        TestData data = createMissionAndProfile();
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var start = new java.util.concurrent.CountDownLatch(1);
            var first = executor.submit(() -> { start.await(); return createEntry(data, "2026-04-10"); });
            var second = executor.submit(() -> { start.await(); return createEntry(data, "2026-04-11"); });
            start.countDown();
            org.assertj.core.api.Assertions.assertThat(first.get(20, java.util.concurrent.TimeUnit.SECONDS))
                    .isNotEqualTo(second.get(20, java.util.concurrent.TimeUnit.SECONDS));
        }
        long id = sheetId(data, 2026, 4);
        mockMvc.perform(get("/api/timesheets/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.timeEntries", hasSize(2)));
    }

    @Test
    void staleMonthlyDecisionShouldNotOverwriteAConcurrentSubmission() throws Exception {
        TestData data = createMissionAndProfile();
        createEntry(data, "2026-04-10");
        long id = sheetId(data, 2026, 4);
        var stale = timesheetRepository.findById(id).orElseThrow();
        mockMvc.perform(post("/api/timesheets/{id}/submit", id)).andExpect(status().isOk());
        stale.setStatus(com.aafkir.tifssi.timesheets.domain.enums.TimesheetStatus.REJECTED);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> timesheetRepository.saveAndFlush(stale))
                .isInstanceOf(org.springframework.dao.OptimisticLockingFailureException.class);
        mockMvc.perform(get("/api/timesheets/{id}", id)).andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    private long sheetId(TestData data, int year, int month) throws Exception {
        var result = mockMvc.perform(get("/api/timesheets").param("profileId", data.profile().getId().toString())
                .param("year", String.valueOf(year)).param("month", String.valueOf(month)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get(0).path("id").asLong();
    }

    private void assertEntryLocked(TestData data, long entry) throws Exception {
        mockMvc.perform(patch("/api/time-entries/{id}", entry).contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":0.5}"))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/time-entries/{id}", entry)).andExpect(status().isConflict());
        mockMvc.perform(post("/api/time-entries").contentType(MediaType.APPLICATION_JSON).content("""
                {"profileId":%d,"missionId":%d,"workDate":"2026-04-12","quantity":1,"unitType":"DAY"}
                """.formatted(data.profile().getId(), data.mission().getId()))).andExpect(status().isConflict());
    }

    private long createEntry(TestData data, String workDate) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"missionId": %d, "profileId": %d, "workDate": "%s",
                                 "quantity": 1.00, "unitType": "DAY", "status": "DRAFT"}
                                """.formatted(data.mission().getId(), data.profile().getId(), workDate)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asLong();
    }

    private TestData createMissionAndProfile() {
        Company company = new Company();
        company.setLegalName("Acme Conseil");
        company = companyRepository.save(company);

        Profile profile = new Profile();
        profile.setType(ProfileType.INTERNAL);
        profile.setFirstName("Lea");
        profile.setLastName("Martin");
        profile.setEmailAddress("lea-" + company.getId() + "@example.com");
        profile.setActive(true);
        profile = profileRepository.save(profile);

        Project project = new Project();
        project.setCompany(company);
        project.setProjectCode("PRJ-TS-" + company.getId());
        project.setProjectName("Timesheets MVP");
        project.setStatus(ProjectStatus.ACTIVE);
        project = projectRepository.save(project);

        Mission mission = new Mission();
        mission.setProject(project);
        mission.setProfile(profile);
        mission.setRoleName("Developer");
        mission.setStartDate(java.time.LocalDate.of(2025, 1, 1));
        mission.setEndDate(java.time.LocalDate.of(2027, 12, 31));
        mission.setStatus(MissionStatus.ACTIVE);
        mission = missionRepository.save(mission);

        return new TestData(profile, mission);
    }

    private record TestData(Profile profile, Mission mission) {
    }
}
