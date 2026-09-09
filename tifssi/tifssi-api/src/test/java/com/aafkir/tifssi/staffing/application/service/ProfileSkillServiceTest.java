package com.aafkir.tifssi.staffing.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.staffing.api.dto.response.ProfileSkillResponse;
import com.aafkir.tifssi.staffing.domain.model.ProfileSkill;
import com.aafkir.tifssi.staffing.domain.model.Skill;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileSkillRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileSkillServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProfileSkillRepository profileSkillRepository;

    @InjectMocks
    private ProfileSkillService profileSkillService;

    @Test
    void findAllByProfileIdShouldReturnProfileSkillDetails() {
        Skill skill = new Skill();
        skill.setId(12L);
        skill.setSkillName("PostgreSQL");

        ProfileSkill profileSkill = new ProfileSkill();
        profileSkill.setSkill(skill);
        profileSkill.setProficiencyLevel(5);
        profileSkill.setYearsOfExperience(8);
        profileSkill.setPrimarySkill(true);

        when(profileRepository.existsById(1L)).thenReturn(true);
        when(profileSkillRepository.findAllByProfileIdWithSkill(1L)).thenReturn(List.of(profileSkill));

        List<ProfileSkillResponse> response = profileSkillService.findAllByProfileId(1L);

        assertThat(response).containsExactly(new ProfileSkillResponse(12L, "PostgreSQL", 5, 8, true));
        verify(profileSkillRepository).findAllByProfileIdWithSkill(1L);
    }

    @Test
    void findAllByProfileIdShouldRejectUnknownProfile() {
        when(profileRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> profileSkillService.findAllByProfileId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Profile with id 99 was not found.");

        verify(profileSkillRepository, never()).findAllByProfileIdWithSkill(99L);
    }
}
