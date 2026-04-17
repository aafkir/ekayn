package com.aafkir.tifssi.staffing.application.service;

import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.util.JsonNullableUtils;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.api.dto.request.ProfileCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.request.ProfilePatchRequest;
import com.aafkir.tifssi.staffing.api.dto.response.ProfileResponse;
import com.aafkir.tifssi.staffing.api.mapper.ProfileApiMapper;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileApiMapper profileApiMapper;
    private final EntityValidationService entityValidationService;

    public ProfileService(
            ProfileRepository profileRepository,
            ProfileApiMapper profileApiMapper,
            EntityValidationService entityValidationService
    ) {
        this.profileRepository = profileRepository;
        this.profileApiMapper = profileApiMapper;
        this.entityValidationService = entityValidationService;
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> findAll() {
        return profileRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(profileApiMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfileResponse findById(Long id) {
        return profileApiMapper.toResponse(getProfile(id));
    }

    public ProfileResponse create(ProfileCreateRequest request) {
        Profile profile = profileApiMapper.toEntity(request);
        entityValidationService.validate(profile);
        return profileApiMapper.toResponse(profileRepository.save(profile));
    }

    public ProfileResponse patch(Long id, ProfilePatchRequest request) {
        Profile profile = getProfile(id);

        if (JsonNullableUtils.isDefined(request.getType())) {
            profile.setType(JsonNullableUtils.unwrap(request.getType()));
        }
        if (JsonNullableUtils.isDefined(request.getFirstName())) {
            profile.setFirstName(JsonNullableUtils.unwrap(request.getFirstName()));
        }
        if (JsonNullableUtils.isDefined(request.getLastName())) {
            profile.setLastName(JsonNullableUtils.unwrap(request.getLastName()));
        }
        if (JsonNullableUtils.isDefined(request.getEmailAddress())) {
            profile.setEmailAddress(JsonNullableUtils.unwrap(request.getEmailAddress()));
        }
        if (JsonNullableUtils.isDefined(request.getPhoneNumber())) {
            profile.setPhoneNumber(JsonNullableUtils.unwrap(request.getPhoneNumber()));
        }
        if (JsonNullableUtils.isDefined(request.getJobTitle())) {
            profile.setJobTitle(JsonNullableUtils.unwrap(request.getJobTitle()));
        }
        if (JsonNullableUtils.isDefined(request.getSeniorityLabel())) {
            profile.setSeniorityLabel(JsonNullableUtils.unwrap(request.getSeniorityLabel()));
        }
        if (JsonNullableUtils.isDefined(request.getActive())) {
            Boolean active = JsonNullableUtils.unwrap(request.getActive());
            if (active == null) {
                throw new IllegalArgumentException("active cannot be null.");
            }
            profile.setActive(active);
        }
        if (JsonNullableUtils.isDefined(request.getDefaultDailyRate())) {
            profile.setDefaultDailyRate(JsonNullableUtils.unwrap(request.getDefaultDailyRate()));
        }
        if (JsonNullableUtils.isDefined(request.getAvailabilityDate())) {
            profile.setAvailabilityDate(JsonNullableUtils.unwrap(request.getAvailabilityDate()));
        }

        entityValidationService.validate(profile);
        return profileApiMapper.toResponse(profileRepository.save(profile));
    }

    public void delete(Long id) {
        profileRepository.delete(getProfile(id));
    }

    public Profile getProfile(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", id));
    }
}
