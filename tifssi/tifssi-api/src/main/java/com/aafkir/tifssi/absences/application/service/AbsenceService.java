package com.aafkir.tifssi.absences.application.service;

import com.aafkir.tifssi.absences.api.dto.request.AbsenceCreateRequest;
import com.aafkir.tifssi.absences.api.dto.request.AbsencePatchRequest;
import com.aafkir.tifssi.absences.api.dto.response.AbsenceResponse;
import com.aafkir.tifssi.absences.api.dto.response.AbsenceSummaryResponse;
import com.aafkir.tifssi.absences.api.mapper.AbsenceApiMapper;
import com.aafkir.tifssi.absences.domain.model.Absence;
import com.aafkir.tifssi.absences.domain.enums.AbsenceStatus;
import com.aafkir.tifssi.absences.domain.enums.AbsenceType;
import com.aafkir.tifssi.absences.infrastructure.repository.AbsenceRepository;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.util.JsonNullableUtils;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.application.service.ProfileService;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AbsenceService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final AbsenceRepository absenceRepository;
    private final ProfileService profileService;
    private final AbsenceApiMapper absenceApiMapper;
    private final EntityValidationService entityValidationService;

    public AbsenceService(
            AbsenceRepository absenceRepository,
            ProfileService profileService,
            AbsenceApiMapper absenceApiMapper,
            EntityValidationService entityValidationService
    ) {
        this.absenceRepository = absenceRepository;
        this.profileService = profileService;
        this.absenceApiMapper = absenceApiMapper;
        this.entityValidationService = entityValidationService;
    }

    public AbsenceResponse create(AbsenceCreateRequest request) {
        Absence absence = absenceApiMapper.toEntity(request);
        absence.setProfile(profileService.getProfile(request.profileId()));
        absence.setComment(normalizeComment(request.comment()));

        validateAbsence(absence);
        entityValidationService.validate(absence);
        return absenceApiMapper.toResponse(absenceRepository.save(absence));
    }

    public AbsenceResponse patch(Long id, AbsencePatchRequest request) {
        Absence absence = getAbsence(id);

        if (JsonNullableUtils.isDefined(request.getProfileId())) {
            Long profileId = JsonNullableUtils.unwrap(request.getProfileId());
            Profile profile = profileId == null ? null : profileService.getProfile(profileId);
            absence.setProfile(profile);
        }
        if (JsonNullableUtils.isDefined(request.getType())) {
            absence.setType(JsonNullableUtils.unwrap(request.getType()));
        }
        if (JsonNullableUtils.isDefined(request.getStartDate())) {
            absence.setStartDate(JsonNullableUtils.unwrap(request.getStartDate()));
        }
        if (JsonNullableUtils.isDefined(request.getEndDate())) {
            absence.setEndDate(JsonNullableUtils.unwrap(request.getEndDate()));
        }
        if (JsonNullableUtils.isDefined(request.getQuantity())) {
            absence.setQuantity(JsonNullableUtils.unwrap(request.getQuantity()));
        }
        if (JsonNullableUtils.isDefined(request.getComment())) {
            absence.setComment(normalizeComment(JsonNullableUtils.unwrap(request.getComment())));
        }
        if (JsonNullableUtils.isDefined(request.getStatus())) {
            absence.setStatus(JsonNullableUtils.unwrap(request.getStatus()));
        }

        validateAbsence(absence);
        entityValidationService.validate(absence);
        return absenceApiMapper.toResponse(absenceRepository.save(absence));
    }

    public void delete(Long id) {
        absenceRepository.delete(getAbsence(id));
    }

    @Transactional(readOnly = true)
    public List<AbsenceResponse> findAll(Long profileId, AbsenceStatus status, AbsenceType type, LocalDate startDate, LocalDate endDate) {
        validateQueryPeriod(startDate, endDate);
        if (profileId != null) profileService.getProfile(profileId);
        List<Absence> source = profileId == null
                ? absenceRepository.findAllByOrderByStartDateAscIdAsc()
                : absenceRepository.findAllByProfileIdOrderByStartDateAscIdAsc(profileId);
        return source
                .stream()
                .filter(absence -> status == null || absence.getStatus() == status)
                .filter(absence -> type == null || absence.getType() == type)
                .filter(absence -> overlaps(absence, startDate, endDate))
                .map(absenceApiMapper::toResponse)
                .toList();
    }

    public List<AbsenceResponse> findAll(Long profileId, LocalDate startDate, LocalDate endDate) {
        return findAll(profileId, null, null, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public AbsenceSummaryResponse getSummary(Long profileId, Integer year) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required.");
        }
        if (year == null || year < 2000) {
            throw new IllegalArgumentException("year must be greater than or equal to 2000.");
        }

        profileService.getProfile(profileId);

        LocalDate periodStart = LocalDate.of(year, 1, 1);
        LocalDate periodEnd = LocalDate.of(year, 12, 31);
        List<Absence> absences = absenceRepository.findAllByProfileIdOrderByStartDateAscIdAsc(profileId)
                .stream()
                .filter(absence -> overlaps(absence, periodStart, periodEnd))
                .toList();

        BigDecimal totalQuantity = absences.stream()
                .map(absence -> quantityWithinPeriod(absence, periodStart, periodEnd))
                .reduce(ZERO, BigDecimal::add);

        return new AbsenceSummaryResponse(profileId, year, absences.size(), totalQuantity);
    }

    public Absence getAbsence(Long id) {
        return absenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Absence", id));
    }

    private void validateAbsence(Absence absence) {
        if (absence.getProfile() == null) {
            throw new IllegalArgumentException("profileId cannot be null.");
        }

        LocalDate startDate = absence.getStartDate();
        LocalDate endDate = absence.getEndDate();
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be greater than or equal to startDate.");
        }

        if (absence.getQuantity() != null && startDate != null && endDate != null) {
            BigDecimal maxQuantity = BigDecimal.valueOf(ChronoUnit.DAYS.between(startDate, endDate) + 1);
            if (absence.getQuantity().compareTo(maxQuantity) > 0) {
                throw new IllegalArgumentException("quantity cannot exceed the number of days in the selected period.");
            }
        }
    }

    private void validateQueryPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be greater than or equal to startDate.");
        }
    }

    private boolean overlaps(Absence absence, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && absence.getEndDate().isBefore(startDate)) {
            return false;
        }
        if (endDate != null && absence.getStartDate().isAfter(endDate)) {
            return false;
        }
        return true;
    }

    private BigDecimal quantityWithinPeriod(Absence absence, LocalDate periodStart, LocalDate periodEnd) {
        LocalDate overlapStart = absence.getStartDate().isAfter(periodStart) ? absence.getStartDate() : periodStart;
        LocalDate overlapEnd = absence.getEndDate().isBefore(periodEnd) ? absence.getEndDate() : periodEnd;
        long overlapDays = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
        long totalDays = ChronoUnit.DAYS.between(absence.getStartDate(), absence.getEndDate()) + 1;

        if (overlapDays == totalDays) {
            return absence.getQuantity();
        }

        return absence.getQuantity()
                .multiply(BigDecimal.valueOf(overlapDays))
                .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP)
                .max(ZERO)
                .min(absence.getQuantity());
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }

        String normalizedComment = comment.trim();
        return normalizedComment.isEmpty() ? null : normalizedComment;
    }
}
