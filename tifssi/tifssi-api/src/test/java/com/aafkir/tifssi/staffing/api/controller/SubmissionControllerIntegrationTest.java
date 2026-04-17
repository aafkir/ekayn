package com.aafkir.tifssi.staffing.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.domain.model.Need;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.infrastructure.repository.NeedRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.SubmissionRepository;
import com.aafkir.tifssi.support.AbstractPostgreSqlIntegrationTest;
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
class SubmissionControllerIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private NeedRepository needRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @AfterEach
    void cleanUp() {
        submissionRepository.deleteAllInBatch();
        needRepository.deleteAllInBatch();
        profileRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
    }

    @Test
    void submissionEndpointsShouldSupportCreateListAndPatchFlow() throws Exception {
        Company company = new Company();
        company.setLegalName("Acme Conseil");
        company = companyRepository.save(company);

        Need need = new Need();
        need.setCompany(company);
        need.setTitle("Consultant Java Senior");
        need.setStatus(NeedStatus.OPEN);
        need = needRepository.save(need);

        Profile profile = new Profile();
        profile.setType(ProfileType.INTERNAL);
        profile.setFirstName("Lea");
        profile.setLastName("Martin");
        profile.setEmailAddress("lea@example.com");
        profile.setActive(true);
        profile = profileRepository.save(profile);

        MvcResult mvcResult = mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "needId": %d,
                                  "profileId": %d,
                                  "proposedDailyRate": 650.00,
                                  "comment": "First shortlist"
                                }
                                """.formatted(need.getId(), profile.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.needId").value(need.getId()))
                .andExpect(jsonPath("$.profileId").value(profile.getId()))
                .andExpect(jsonPath("$.status").value("PRESELECTED"))
                .andExpect(jsonPath("$.submittedAt").doesNotExist())
                .andExpect(jsonPath("$.comment").value("First shortlist"))
                .andReturn();

        long submissionId = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).path("id").asLong();

        mockMvc.perform(get("/api/submissions").param("needId", need.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].profileId").value(profile.getId()));

        mockMvc.perform(get("/api/submissions").param("profileId", profile.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].needId").value(need.getId()));

        mockMvc.perform(get("/api/submissions/{id}", submissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(submissionId))
                .andExpect(jsonPath("$.needId").value(need.getId()))
                .andExpect(jsonPath("$.profileId").value(profile.getId()));

        mockMvc.perform(patch("/api/submissions/{id}/status", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "SENT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.submittedAt", notNullValue()));

        mockMvc.perform(patch("/api/submissions/{id}", submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "comment": "Updated after client call",
                                  "proposedDailyRate": 700.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Updated after client call"))
                .andExpect(jsonPath("$.proposedDailyRate").value(700.00));

        mockMvc.perform(delete("/api/submissions/{id}", submissionId))
                .andExpect(status().isNoContent());
    }

    @Test
    void createShouldRejectDuplicateSubmission() throws Exception {
        Company company = new Company();
        company.setLegalName("Acme Conseil");
        company = companyRepository.save(company);

        Need need = new Need();
        need.setCompany(company);
        need.setTitle("Consultant Java Senior");
        need.setStatus(NeedStatus.OPEN);
        need = needRepository.save(need);

        Profile profile = new Profile();
        profile.setType(ProfileType.EXTERNAL);
        profile.setFirstName("Sara");
        profile.setLastName("Dupont");
        profile.setEmailAddress("sara@example.com");
        profile.setActive(true);
        profile = profileRepository.save(profile);

        mockMvc.perform(post("/api/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "needId": %d,
                          "profileId": %d
                        }
                        """.formatted(need.getId(), profile.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "needId": %d,
                                  "profileId": %d
                                }
                                """.formatted(need.getId(), profile.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("profileId"));
    }
}
