package com.aafkir.tifssi.staffing.application.service;

import com.aafkir.tifssi.shared.api.error.ApiFieldError;
import com.aafkir.tifssi.shared.application.exception.RequestValidationException;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.util.JsonNullableUtils;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.api.dto.request.SubmissionCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.request.SubmissionPatchRequest;
import com.aafkir.tifssi.staffing.api.dto.request.SubmissionStatusPatchRequest;
import com.aafkir.tifssi.staffing.api.dto.response.SubmissionResponse;
import com.aafkir.tifssi.staffing.api.mapper.SubmissionApiMapper;
import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
import com.aafkir.tifssi.staffing.domain.enums.SubmissionStatus;
import com.aafkir.tifssi.staffing.domain.model.Need;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.domain.model.Submission;
import com.aafkir.tifssi.staffing.infrastructure.repository.NeedRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.SubmissionRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final NeedRepository needRepository;
    private final ProfileRepository profileRepository;
    private final SubmissionApiMapper submissionApiMapper;
    private final EntityValidationService entityValidationService;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            NeedRepository needRepository,
            ProfileRepository profileRepository,
            SubmissionApiMapper submissionApiMapper,
            EntityValidationService entityValidationService
    ) {
        this.submissionRepository = submissionRepository;
        this.needRepository = needRepository;
        this.profileRepository = profileRepository;
        this.submissionApiMapper = submissionApiMapper;
        this.entityValidationService = entityValidationService;
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> findAll(Long needId, Long profileId) {
        validateFilters(needId, profileId);

        if (needId != null) {
            getNeed(needId);
            return submissionRepository.findAllByNeedIdOrderByIdAsc(needId)
                    .stream()
                    .map(submissionApiMapper::toResponse)
                    .toList();
        }

        getProfile(profileId);
        return submissionRepository.findAllByProfileIdOrderByIdAsc(profileId)
                .stream()
                .map(submissionApiMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubmissionResponse findById(Long id) {
        return submissionApiMapper.toResponse(getSubmission(id));
    }

    public SubmissionResponse create(SubmissionCreateRequest request) {
        Need need = getNeed(request.needId());
        Profile profile = getProfile(request.profileId());

        validateSubmissionCanBeCreated(need, profile);

        Submission submission = submissionApiMapper.toEntity(request);
        submission.setNeed(need);
        submission.setProfile(profile);
        submission.setStatus(SubmissionStatus.PRESELECTED);
        submission.setNotes(normalizeComment(request.comment()));

        entityValidationService.validate(submission);
        return submissionApiMapper.toResponse(submissionRepository.save(submission));
    }

    public SubmissionResponse patchStatus(Long id, SubmissionStatusPatchRequest request) {
        Submission submission = getSubmission(id);
        SubmissionStatus nextStatus = request.status();

        validateStatusChange(submission, nextStatus);

        submission.setStatus(nextStatus);
        if (submission.getSubmittedAt() == null && nextStatus != SubmissionStatus.PRESELECTED) {
            submission.setSubmittedAt(Instant.now());
        }

        entityValidationService.validate(submission);
        return submissionApiMapper.toResponse(submissionRepository.save(submission));
    }

    public SubmissionResponse patch(Long id, SubmissionPatchRequest request) {
        Submission submission = getSubmission(id);

        if (JsonNullableUtils.isDefined(request.getProposedDailyRate())) {
            submission.setProposedDailyRate(JsonNullableUtils.unwrap(request.getProposedDailyRate()));
        }
        if (JsonNullableUtils.isDefined(request.getComment())) {
            submission.setNotes(normalizeComment(JsonNullableUtils.unwrap(request.getComment())));
        }

        entityValidationService.validate(submission);
        return submissionApiMapper.toResponse(submissionRepository.save(submission));
    }

    public void delete(Long id) {
        submissionRepository.delete(getSubmission(id));
    }

    private Submission getSubmission(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", id));
    }

    private Need getNeed(Long id) {
        return needRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Need", id));
    }

    private Profile getProfile(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", id));
    }

    private void validateFilters(Long needId, Long profileId) {
        if ((needId == null && profileId == null) || (needId != null && profileId != null)) {
            throw new IllegalArgumentException("Exactly one of needId or profileId must be provided.");
        }
    }

    private void validateSubmissionCanBeCreated(Need need, Profile profile) {
        if (!(need.getStatus() == NeedStatus.OPEN
                || need.getStatus() == NeedStatus.SHORTLIST
                || need.getStatus() == NeedStatus.PRESENTED)) {
            throw new RequestValidationException(
                    "Submission request is invalid.",
                    List.of(new ApiFieldError("needId", "Submissions can only be created for OPEN, SHORTLIST or PRESENTED needs."))
            );
        }
        if (!profile.isActive()) {
            throw new RequestValidationException(
                    "Submission request is invalid.",
                    List.of(new ApiFieldError("profileId", "Profile must be active to create a submission."))
            );
        }
        if (submissionRepository.existsByNeedIdAndProfileId(need.getId(), profile.getId())) {
            throw new RequestValidationException(
                    "Submission request is invalid.",
                    List.of(new ApiFieldError("profileId", "A submission already exists for this need and profile."))
            );
        }
    }

    private void validateStatusChange(Submission submission, SubmissionStatus nextStatus) {
        if (nextStatus == SubmissionStatus.WON
                && submissionRepository.existsByNeedIdAndStatusAndIdNot(submission.getNeed().getId(), SubmissionStatus.WON, submission.getId())) {
            throw new RequestValidationException(
                    "Submission request is invalid.",
                    List.of(new ApiFieldError("status", "Another submission for this need is already marked as WON."))
            );
        }
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }

        String normalizedComment = comment.trim();
        if (normalizedComment.isEmpty()) {
            throw new RequestValidationException(
                    "Submission request is invalid.",
                    List.of(new ApiFieldError("comment", "comment must not be blank."))
            );
        }
        return normalizedComment;
    }
}
