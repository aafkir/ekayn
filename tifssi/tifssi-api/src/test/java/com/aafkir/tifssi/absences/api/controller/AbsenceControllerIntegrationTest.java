package com.aafkir.tifssi.absences.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aafkir.tifssi.absences.domain.enums.AbsenceStatus;
import com.aafkir.tifssi.absences.domain.enums.AbsenceType;
import com.aafkir.tifssi.absences.domain.model.Absence;
import com.aafkir.tifssi.absences.infrastructure.repository.AbsenceRepository;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import com.aafkir.tifssi.support.AbstractPostgreSqlIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
class AbsenceControllerIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AbsenceRepository absenceRepository;
    @Autowired
    private ProfileRepository profileRepository;

    @AfterEach
    void cleanUp() {
        absenceRepository.deleteAllInBatch();
        profileRepository.deleteAllInBatch();
    }

    @Test
    void absenceEndpointsShouldSupportCrudListAndSummaryFlow() throws Exception {
        Profile profile = createProfile();
        createAbsence(profile, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 1), new BigDecimal("1.00"), AbsenceType.RTT);

        MvcResult mvcResult = mockMvc.perform(post("/api/absences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileId": %d,
                                  "type": "PAID_LEAVE",
                                  "startDate": "2026-07-10",
                                  "endDate": "2026-07-12",
                                  "quantity": 3.00,
                                  "comment": "Vacances ete",
                                  "status": "SUBMITTED"
                                }
                                """.formatted(profile.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileId").value(profile.getId()))
                .andExpect(jsonPath("$.type").value("PAID_LEAVE"))
                .andExpect(jsonPath("$.quantity").value(3.00))
                .andExpect(jsonPath("$.comment").value("Vacances ete"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andReturn();

        long absenceId = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).path("id").asLong();
        org.assertj.core.api.Assertions.assertThat(mvcResult.getResponse().getHeader("Location"))
                .isEqualTo("http://localhost/api/absences/" + absenceId);

        mockMvc.perform(get("/api/absences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/absences").param("status", "SUBMITTED").param("type", "PAID_LEAVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/absences").param("profileId", profile.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/absences")
                        .param("profileId", profile.getId().toString())
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(absenceId));

        mockMvc.perform(patch("/api/absences/{id}", absenceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 2.50,
                                  "comment": "Ajuste apres validation RH",
                                  "status": "APPROVED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2.50))
                .andExpect(jsonPath("$.comment").value("Ajuste apres validation RH"))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/api/absences/summary")
                        .param("profileId", profile.getId().toString())
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileId").value(profile.getId()))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.totalAbsences").value(2))
                .andExpect(jsonPath("$.totalQuantity").value(3.50));

        mockMvc.perform(delete("/api/absences/{id}", absenceId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/absences")
                        .param("profileId", profile.getId().toString())
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createShouldRejectInvalidDateRange() throws Exception {
        Profile profile = createProfile();

        mockMvc.perform(post("/api/absences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileId": %d,
                                  "type": "SICK_LEAVE",
                                  "startDate": "2026-07-05",
                                  "endDate": "2026-07-03",
                                  "quantity": 1.00,
                                  "status": "DRAFT"
                                }
                                """.formatted(profile.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("endDate must be greater than or equal to startDate."));
    }

    private Profile createProfile() {
        Profile profile = new Profile();
        profile.setType(ProfileType.INTERNAL);
        profile.setFirstName("Lea");
        profile.setLastName("Martin");
        profile.setEmailAddress("lea@example.com");
        profile.setActive(true);
        return profileRepository.save(profile);
    }

    private Absence createAbsence(
            Profile profile,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal quantity,
            AbsenceType type
    ) {
        Absence absence = new Absence();
        absence.setProfile(profile);
        absence.setType(type);
        absence.setStartDate(startDate);
        absence.setEndDate(endDate);
        absence.setQuantity(quantity);
        absence.setComment("Fixture");
        absence.setStatus(AbsenceStatus.APPROVED);
        return absenceRepository.save(absence);
    }
}
