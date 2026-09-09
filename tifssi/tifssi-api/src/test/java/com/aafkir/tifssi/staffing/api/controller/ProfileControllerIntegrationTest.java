package com.aafkir.tifssi.staffing.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.domain.model.ProfileSkill;
import com.aafkir.tifssi.staffing.domain.model.Skill;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileSkillRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.SkillRepository;
import java.time.LocalDate;
import com.aafkir.tifssi.support.AbstractPostgreSqlIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private ProfileSkillRepository profileSkillRepository;

    @AfterEach
    void cleanUp() {
        profileSkillRepository.deleteAllInBatch();
        skillRepository.deleteAllInBatch();
        profileRepository.deleteAllInBatch();
    }

    @Test
    void createShouldReturnCreatedProfile() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "INTERNAL",
                                  "firstName": "Nina",
                                  "lastName": "Dupont",
                                  "email": "nina.dupont@tifssi.example",
                                  "phone": "+33611121314",
                                  "role": "Lead Backend",
                                  "seniority": "Expert",
                                  "active": true,
                                  "defaultDailyRate": 750.00,
                                  "availabilityDate": "2026-04-22"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.type").value("INTERNAL"))
                .andExpect(jsonPath("$.firstName").value("Nina"))
                .andExpect(jsonPath("$.lastName").value("Dupont"))
                .andExpect(jsonPath("$.emailAddress").value("nina.dupont@tifssi.example"))
                .andExpect(jsonPath("$.phoneNumber").value("+33611121314"))
                .andExpect(jsonPath("$.jobTitle").value("Lead Backend"))
                .andExpect(jsonPath("$.seniorityLabel").value("Expert"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.defaultDailyRate").value(750.00))
                .andExpect(jsonPath("$.availabilityDate").value("2026-04-22"))
                .andReturn();
        long profileId = new ObjectMapper().readTree(result.getResponse().getContentAsString()).path("id").asLong();
        assertThat(profileId).isPositive();
        assertThat(result.getResponse().getHeader("Location"))
                .isEqualTo("http://localhost/api/profiles/" + profileId);
        assertThat(profileRepository.existsById(profileId)).isTrue();
    }

    @Test
    void createShouldRejectInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "INTERNAL",
                                  "firstName": "",
                                  "lastName": "Dupont",
                                  "email": "invalid-email",
                                  "active": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'firstName')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')]").exists());
    }

    @Test
    void findSkillsByProfileIdShouldReturnProfileSkills() throws Exception {
        Profile profile = new Profile();
        profile.setType(ProfileType.EXTERNAL);
        profile.setFirstName("Sara");
        profile.setLastName("Benhamou");
        profile.setEmailAddress("sara.benhamou@tifssi.example");
        profile.setActive(true);
        profile.setAvailabilityDate(LocalDate.of(2026, 9, 4));
        profile = profileRepository.save(profile);

        Skill skill = new Skill();
        skill.setSkillCode("FIGMA");
        skill.setSkillName("Figma");
        skill = skillRepository.save(skill);

        ProfileSkill profileSkill = new ProfileSkill();
        profileSkill.setProfile(profile);
        profileSkill.setSkill(skill);
        profileSkill.setProficiencyLevel(5);
        profileSkill.setYearsOfExperience(10);
        profileSkill.setPrimarySkill(true);
        profileSkillRepository.save(profileSkill);

        mockMvc.perform(get("/api/profiles/{profileId}/skills", profile.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skillId").value(skill.getId()))
                .andExpect(jsonPath("$[0].name").value("Figma"))
                .andExpect(jsonPath("$[0].level").value(5))
                .andExpect(jsonPath("$[0].yearsOfExperience").value(10))
                .andExpect(jsonPath("$[0].primarySkill").value(true));
    }

    @Test
    void findSkillsByProfileIdShouldReturnNotFoundForUnknownProfile() throws Exception {
        mockMvc.perform(get("/api/profiles/{profileId}/skills", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchShouldUpdateProfileUsingApiFieldNames() throws Exception {
        Profile profile = createProfile("Nina", "Dupont", "nina.dupont@tifssi.example");

        mockMvc.perform(patch("/api/profiles/{profileId}", profile.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "EXTERNAL",
                                  "firstName": "Lea",
                                  "lastName": "Martin",
                                  "email": "lea.martin@tifssi.example",
                                  "phone": "+33615161718",
                                  "role": "Lead Backend",
                                  "seniority": "Expert",
                                  "active": false,
                                  "defaultDailyRate": 750.00,
                                  "availabilityDate": "2026-04-22"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("EXTERNAL"))
                .andExpect(jsonPath("$.firstName").value("Lea"))
                .andExpect(jsonPath("$.lastName").value("Martin"))
                .andExpect(jsonPath("$.emailAddress").value("lea.martin@tifssi.example"))
                .andExpect(jsonPath("$.phoneNumber").value("+33615161718"))
                .andExpect(jsonPath("$.jobTitle").value("Lead Backend"))
                .andExpect(jsonPath("$.seniorityLabel").value("Expert"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.defaultDailyRate").value(750.00))
                .andExpect(jsonPath("$.availabilityDate").value("2026-04-22"));
    }

    @Test
    void patchShouldReturnNotFoundForUnknownProfile() throws Exception {
        mockMvc.perform(patch("/api/profiles/{profileId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"Lead Backend\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchShouldRejectInvalidProfileState() throws Exception {
        Profile profile = createProfile("Nina", "Dupont", "nina.dupont@tifssi.example");

        mockMvc.perform(patch("/api/profiles/{profileId}", profile.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"invalid-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'emailAddress')]").exists());
    }

    private Profile createProfile(String firstName, String lastName, String email) {
        Profile profile = new Profile();
        profile.setType(ProfileType.INTERNAL);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setEmailAddress(email);
        profile.setActive(true);
        return profileRepository.save(profile);
    }
}
