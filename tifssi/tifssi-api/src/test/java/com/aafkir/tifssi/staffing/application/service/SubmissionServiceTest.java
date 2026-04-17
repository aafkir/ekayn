package com.aafkir.tifssi.staffing.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.shared.api.error.ApiFieldError;
import com.aafkir.tifssi.shared.application.exception.RequestValidationException;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.api.dto.request.SubmissionCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.request.SubmissionPatchRequest;
import com.aafkir.tifssi.staffing.api.dto.request.SubmissionStatusPatchRequest;
import com.aafkir.tifssi.staffing.api.dto.response.SubmissionResponse;
import com.aafkir.tifssi.staffing.api.mapper.SubmissionApiMapper;
import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.domain.enums.SubmissionStatus;
import com.aafkir.tifssi.staffing.domain.model.Need;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.domain.model.Submission;
import com.aafkir.tifssi.staffing.infrastructure.repository.NeedRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.SubmissionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private NeedRepository needRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private SubmissionApiMapper submissionApiMapper;
    @Mock
    private EntityValidationService entityValidationService;

    @InjectMocks
    private SubmissionService submissionService;

    private Need need;
    private Profile profile;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setId(10L);
        company.setLegalName("Acme Conseil");

        need = new Need();
        need.setId(1L);
        need.setCompany(company);
        need.setTitle("Consultant Java");
        need.setStatus(NeedStatus.OPEN);

        profile = new Profile();
        profile.setId(2L);
        profile.setType(ProfileType.INTERNAL);
        profile.setFirstName("Lea");
        profile.setLastName("Martin");
        profile.setEmailAddress("lea@example.com");
        profile.setActive(true);
    }

    @Test
    void createShouldPersistPreselectedSubmission() {
        SubmissionCreateRequest request = new SubmissionCreateRequest(1L, 2L, new BigDecimal("650.00"), "  First shortlist  ");
        Submission mappedSubmission = new Submission();
        mappedSubmission.setProposedDailyRate(request.proposedDailyRate());

        when(needRepository.findById(1L)).thenReturn(Optional.of(need));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(profile));
        when(submissionApiMapper.toEntity(request)).thenReturn(mappedSubmission);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission submission = invocation.getArgument(0);
            submission.setId(99L);
            return submission;
        });
        when(submissionApiMapper.toResponse(any(Submission.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        SubmissionResponse response = submissionService.create(request);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.needId()).isEqualTo(1L);
        assertThat(response.profileId()).isEqualTo(2L);
        assertThat(response.status()).isEqualTo(SubmissionStatus.PRESELECTED);
        assertThat(response.proposedDailyRate()).isEqualByComparingTo("650.00");
        assertThat(response.comment()).isEqualTo("First shortlist");
        assertThat(response.submittedAt()).isNull();

        ArgumentCaptor<Submission> submissionCaptor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository).save(submissionCaptor.capture());
        Submission savedSubmission = submissionCaptor.getValue();
        assertThat(savedSubmission.getNeed()).isSameAs(need);
        assertThat(savedSubmission.getProfile()).isSameAs(profile);
        assertThat(savedSubmission.getStatus()).isEqualTo(SubmissionStatus.PRESELECTED);
        assertThat(savedSubmission.getNotes()).isEqualTo("First shortlist");
    }

    @Test
    void findAllShouldRequireExactlyOneFilter() {
        assertThatThrownBy(() -> submissionService.findAll(null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly one of needId or profileId must be provided.");

        assertThatThrownBy(() -> submissionService.findAll(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exactly one of needId or profileId must be provided.");
    }

    @Test
    void createShouldRejectDuplicateNeedAndProfile() {
        SubmissionCreateRequest request = new SubmissionCreateRequest(1L, 2L, null, null);

        when(needRepository.findById(1L)).thenReturn(Optional.of(need));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(profile));
        when(submissionRepository.existsByNeedIdAndProfileId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> submissionService.create(request))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("Submission request is invalid.");

        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void createShouldRejectClosedNeed() {
        need.setStatus(NeedStatus.WON);
        SubmissionCreateRequest request = new SubmissionCreateRequest(1L, 2L, null, null);

        when(needRepository.findById(1L)).thenReturn(Optional.of(need));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> submissionService.create(request))
                .isInstanceOfSatisfying(RequestValidationException.class, exception ->
                        assertThat(exception.getFieldErrors())
                                .containsExactly(new ApiFieldError(
                                        "needId",
                                        "Submissions can only be created for OPEN, SHORTLIST or PRESENTED needs."
                                )));

        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void createShouldRejectInactiveProfile() {
        profile.setActive(false);
        SubmissionCreateRequest request = new SubmissionCreateRequest(1L, 2L, null, null);

        when(needRepository.findById(1L)).thenReturn(Optional.of(need));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> submissionService.create(request))
                .isInstanceOfSatisfying(RequestValidationException.class, exception ->
                        assertThat(exception.getFieldErrors())
                                .containsExactly(new ApiFieldError(
                                        "profileId",
                                        "Profile must be active to create a submission."
                                )));

        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void createShouldRejectBlankComment() {
        SubmissionCreateRequest request = new SubmissionCreateRequest(1L, 2L, null, "   ");
        Submission mappedSubmission = new Submission();

        when(needRepository.findById(1L)).thenReturn(Optional.of(need));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(profile));
        when(submissionApiMapper.toEntity(request)).thenReturn(mappedSubmission);

        assertThatThrownBy(() -> submissionService.create(request))
                .isInstanceOfSatisfying(RequestValidationException.class, exception ->
                        assertThat(exception.getFieldErrors())
                                .containsExactly(new ApiFieldError("comment", "comment must not be blank.")));

        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void patchStatusShouldSetSubmittedAtWhenMovingForward() {
        Submission submission = new Submission();
        submission.setId(5L);
        submission.setNeed(need);
        submission.setProfile(profile);
        submission.setStatus(SubmissionStatus.PRESELECTED);

        when(submissionRepository.findById(5L)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(submissionApiMapper.toResponse(any(Submission.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        SubmissionResponse response = submissionService.patchStatus(5L, new SubmissionStatusPatchRequest(SubmissionStatus.SENT));

        assertThat(response.status()).isEqualTo(SubmissionStatus.SENT);
        assertThat(response.submittedAt()).isNotNull();
    }

    @Test
    void patchStatusShouldRejectSecondWonSubmissionForSameNeed() {
        Submission submission = new Submission();
        submission.setId(5L);
        submission.setNeed(need);
        submission.setProfile(profile);
        submission.setStatus(SubmissionStatus.SENT);

        when(submissionRepository.findById(5L)).thenReturn(Optional.of(submission));
        when(submissionRepository.existsByNeedIdAndStatusAndIdNot(1L, SubmissionStatus.WON, 5L)).thenReturn(true);

        assertThatThrownBy(() -> submissionService.patchStatus(5L, new SubmissionStatusPatchRequest(SubmissionStatus.WON)))
                .isInstanceOfSatisfying(RequestValidationException.class, exception ->
                        assertThat(exception.getFieldErrors())
                                .containsExactly(new ApiFieldError(
                                        "status",
                                        "Another submission for this need is already marked as WON."
                                )));

        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void patchShouldUpdateCommentAndRate() {
        Submission submission = new Submission();
        submission.setId(5L);
        submission.setNeed(need);
        submission.setProfile(profile);
        submission.setStatus(SubmissionStatus.SENT);

        SubmissionPatchRequest request = new SubmissionPatchRequest();
        request.setProposedDailyRate(JsonNullable.of(new BigDecimal("700.00")));
        request.setComment(JsonNullable.of("  Updated note "));

        when(submissionRepository.findById(5L)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(submissionApiMapper.toResponse(any(Submission.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        SubmissionResponse response = submissionService.patch(5L, request);

        assertThat(response.proposedDailyRate()).isEqualByComparingTo("700.00");
        assertThat(response.comment()).isEqualTo("Updated note");
    }

    private SubmissionResponse toResponse(Submission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getNeed() == null ? null : submission.getNeed().getId(),
                submission.getProfile() == null ? null : submission.getProfile().getId(),
                submission.getStatus(),
                submission.getSubmittedAt(),
                submission.getProposedDailyRate(),
                submission.getNotes(),
                submission.getCreatedAt(),
                submission.getUpdatedAt()
        );
    }
}
