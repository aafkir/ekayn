package com.aafkir.tifssi.staffing.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.staffing.api.dto.request.ProfileCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.request.ProfilePatchRequest;
import com.aafkir.tifssi.staffing.api.dto.response.ProfileResponse;
import com.aafkir.tifssi.staffing.api.mapper.ProfileApiMapper;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProfileApiMapper profileApiMapper;
    @Mock
    private EntityValidationService entityValidationService;

    @InjectMocks
    private ProfileService profileService;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = new Profile();
        profile.setId(1L);
        profile.setType(ProfileType.INTERNAL);
        profile.setFirstName("Nina");
        profile.setLastName("Dupont");
        profile.setEmailAddress("nina.dupont@tifssi.example");
        profile.setPhoneNumber("+33611121314");
        profile.setJobTitle("Developpeuse backend");
        profile.setSeniorityLabel("Senior");
        profile.setActive(true);
        profile.setDefaultDailyRate(new BigDecimal("700.00"));
        profile.setAvailabilityDate(LocalDate.of(2026, 4, 15));
    }

    @Test
    void createShouldValidateAndPersistProfile() {
        ProfileCreateRequest request = new ProfileCreateRequest(
                ProfileType.INTERNAL,
                "Nina",
                "Dupont",
                "nina.dupont@tifssi.example",
                "+33611121314",
                "Developpeuse backend",
                "Senior",
                true,
                new BigDecimal("700.00"),
                LocalDate.of(2026, 4, 15)
        );
        Profile mappedProfile = new Profile();
        mappedProfile.setType(request.type());
        mappedProfile.setFirstName(request.firstName());
        mappedProfile.setLastName(request.lastName());
        mappedProfile.setEmailAddress(request.email());
        mappedProfile.setPhoneNumber(request.phone());
        mappedProfile.setJobTitle(request.role());
        mappedProfile.setSeniorityLabel(request.seniority());
        mappedProfile.setActive(request.active());
        mappedProfile.setDefaultDailyRate(request.defaultDailyRate());
        mappedProfile.setAvailabilityDate(request.availabilityDate());

        when(profileApiMapper.toEntity(request)).thenReturn(mappedProfile);
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
            Profile savedProfile = invocation.getArgument(0);
            savedProfile.setId(51L);
            return savedProfile;
        });
        when(profileApiMapper.toResponse(any(Profile.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        ProfileResponse response = profileService.create(request);

        assertThat(response.id()).isEqualTo(51L);
        assertThat(response.type()).isEqualTo(ProfileType.INTERNAL);
        assertThat(response.defaultDailyRate()).isEqualByComparingTo("700.00");
        verify(entityValidationService).validate(mappedProfile);
    }

    @Test
    void patchShouldUpdateMutableFields() {
        ProfilePatchRequest request = new ProfilePatchRequest();
        request.setType(JsonNullable.of(ProfileType.EXTERNAL));
        request.setFirstName(JsonNullable.of("Lea"));
        request.setLastName(JsonNullable.of("Martin"));
        request.setEmail(JsonNullable.of("lea.martin@tifssi.example"));
        request.setPhone(JsonNullable.of("+33615161718"));
        request.setRole(JsonNullable.of("Lead Backend"));
        request.setSeniority(JsonNullable.of("Expert"));
        request.setActive(JsonNullable.of(false));
        request.setDefaultDailyRate(JsonNullable.of(new BigDecimal("750.00")));
        request.setAvailabilityDate(JsonNullable.of(LocalDate.of(2026, 4, 22)));

        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileApiMapper.toResponse(any(Profile.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        ProfileResponse response = profileService.patch(1L, request);

        assertThat(response.type()).isEqualTo(ProfileType.EXTERNAL);
        assertThat(response.firstName()).isEqualTo("Lea");
        assertThat(response.lastName()).isEqualTo("Martin");
        assertThat(response.emailAddress()).isEqualTo("lea.martin@tifssi.example");
        assertThat(response.phoneNumber()).isEqualTo("+33615161718");
        assertThat(response.jobTitle()).isEqualTo("Lead Backend");
        assertThat(response.seniorityLabel()).isEqualTo("Expert");
        assertThat(response.active()).isFalse();
        assertThat(response.defaultDailyRate()).isEqualByComparingTo("750.00");
        assertThat(response.availabilityDate()).isEqualTo(LocalDate.of(2026, 4, 22));
        verify(entityValidationService).validate(profile);
    }

    @Test
    void patchShouldRejectNullActiveFlag() {
        ProfilePatchRequest request = new ProfilePatchRequest();
        request.setActive(JsonNullable.of(null));

        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> profileService.patch(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("active cannot be null.");

        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void patchShouldRejectUnknownProfile() {
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.patch(99L, new ProfilePatchRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Profile with id 99 was not found.");

        verify(profileRepository, never()).save(any(Profile.class));
    }

    private ProfileResponse toResponse(Profile target) {
        return new ProfileResponse(
                target.getId(),
                target.getType(),
                target.getFirstName(),
                target.getLastName(),
                target.getEmailAddress(),
                target.getPhoneNumber(),
                target.getJobTitle(),
                target.getSeniorityLabel(),
                target.isActive(),
                target.getDefaultDailyRate(),
                target.getAvailabilityDate(),
                target.getCreatedAt(),
                target.getUpdatedAt()
        );
    }
}
