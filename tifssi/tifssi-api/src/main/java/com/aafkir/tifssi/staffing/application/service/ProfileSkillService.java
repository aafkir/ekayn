package com.aafkir.tifssi.staffing.application.service;

import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.staffing.api.dto.response.ProfileSkillResponse;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileSkillRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileSkillService {

    private final ProfileRepository profileRepository;
    private final ProfileSkillRepository profileSkillRepository;

    public ProfileSkillService(ProfileRepository profileRepository, ProfileSkillRepository profileSkillRepository) {
        this.profileRepository = profileRepository;
        this.profileSkillRepository = profileSkillRepository;
    }

    public List<ProfileSkillResponse> findAllByProfileId(Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("Profile", profileId);
        }

        return profileSkillRepository.findAllByProfileIdWithSkill(profileId)
                .stream()
                .map(profileSkill -> new ProfileSkillResponse(
                        profileSkill.getSkill().getId(),
                        profileSkill.getSkill().getSkillName(),
                        profileSkill.getProficiencyLevel(),
                        profileSkill.getYearsOfExperience(),
                        profileSkill.isPrimarySkill()
                ))
                .toList();
    }
}
