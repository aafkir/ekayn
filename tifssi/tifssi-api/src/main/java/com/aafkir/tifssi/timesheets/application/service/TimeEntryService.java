package com.aafkir.tifssi.timesheets.application.service;

import com.aafkir.tifssi.projects.application.service.MissionService;
import com.aafkir.tifssi.projects.domain.model.Mission;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.util.JsonNullableUtils;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.application.service.ProfileService;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.timesheets.api.dto.request.TimeEntryCreateRequest;
import com.aafkir.tifssi.timesheets.api.dto.request.TimeEntryPatchRequest;
import com.aafkir.tifssi.timesheets.api.dto.response.TimeEntryResponse;
import com.aafkir.tifssi.timesheets.api.dto.response.TimeEntrySummaryResponse;
import com.aafkir.tifssi.timesheets.api.mapper.TimeEntryApiMapper;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryUnitType;
import com.aafkir.tifssi.timesheets.domain.model.TimeEntry;
import com.aafkir.tifssi.timesheets.infrastructure.repository.TimeEntryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TimeEntryService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final TimeEntryRepository timeEntryRepository;
    private final MissionService missionService;
    private final ProfileService profileService;
    private final TimeEntryApiMapper timeEntryApiMapper;
    private final EntityValidationService entityValidationService;

    public TimeEntryService(
            TimeEntryRepository timeEntryRepository,
            MissionService missionService,
            ProfileService profileService,
            TimeEntryApiMapper timeEntryApiMapper,
            EntityValidationService entityValidationService
    ) {
        this.timeEntryRepository = timeEntryRepository;
        this.missionService = missionService;
        this.profileService = profileService;
        this.timeEntryApiMapper = timeEntryApiMapper;
        this.entityValidationService = entityValidationService;
    }

    public TimeEntryResponse create(TimeEntryCreateRequest request) {
        TimeEntry timeEntry = timeEntryApiMapper.toEntity(request);
        timeEntry.setMission(missionService.getMission(request.missionId()));
        timeEntry.setProfile(profileService.getProfile(request.profileId()));
        timeEntry.setComment(normalizeComment(request.comment()));

        validateTimeEntryRelations(timeEntry);
        entityValidationService.validate(timeEntry);
        return timeEntryApiMapper.toResponse(timeEntryRepository.save(timeEntry));
    }

    public TimeEntryResponse patch(Long id, TimeEntryPatchRequest request) {
        TimeEntry timeEntry = getTimeEntry(id);

        if (JsonNullableUtils.isDefined(request.getMissionId())) {
            Long missionId = JsonNullableUtils.unwrap(request.getMissionId());
            Mission mission = missionId == null ? null : missionService.getMission(missionId);
            timeEntry.setMission(mission);
        }
        if (JsonNullableUtils.isDefined(request.getProfileId())) {
            Long profileId = JsonNullableUtils.unwrap(request.getProfileId());
            Profile profile = profileId == null ? null : profileService.getProfile(profileId);
            timeEntry.setProfile(profile);
        }
        if (JsonNullableUtils.isDefined(request.getWorkDate())) {
            timeEntry.setWorkDate(JsonNullableUtils.unwrap(request.getWorkDate()));
        }
        if (JsonNullableUtils.isDefined(request.getQuantity())) {
            timeEntry.setQuantity(JsonNullableUtils.unwrap(request.getQuantity()));
        }
        if (JsonNullableUtils.isDefined(request.getUnitType())) {
            timeEntry.setUnitType(JsonNullableUtils.unwrap(request.getUnitType()));
        }
        if (JsonNullableUtils.isDefined(request.getComment())) {
            timeEntry.setComment(normalizeComment(JsonNullableUtils.unwrap(request.getComment())));
        }
        if (JsonNullableUtils.isDefined(request.getStatus())) {
            timeEntry.setStatus(JsonNullableUtils.unwrap(request.getStatus()));
        }

        validateTimeEntryRelations(timeEntry);
        entityValidationService.validate(timeEntry);
        return timeEntryApiMapper.toResponse(timeEntryRepository.save(timeEntry));
    }

    public void delete(Long id) {
        timeEntryRepository.delete(getTimeEntry(id));
    }

    @Transactional(readOnly = true)
    public List<TimeEntryResponse> findAll(Long missionId, Long profileId) {
        validateListFilters(missionId, profileId);

        if (missionId != null) {
            missionService.getMission(missionId);
            return timeEntryRepository.findAllByMissionIdOrderByWorkDateAscIdAsc(missionId)
                    .stream()
                    .map(timeEntryApiMapper::toResponse)
                    .toList();
        }

        profileService.getProfile(profileId);
        return timeEntryRepository.findAllByProfileIdOrderByWorkDateAscIdAsc(profileId)
                .stream()
                .map(timeEntryApiMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TimeEntrySummaryResponse getSummary(Long missionId, Integer month, Integer year) {
        if (missionId == null) {
            throw new IllegalArgumentException("missionId is required.");
        }
        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12.");
        }
        if (year == null || year < 2000) {
            throw new IllegalArgumentException("year must be greater than or equal to 2000.");
        }

        missionService.getMission(missionId);

        YearMonth yearMonth = YearMonth.of(year, month);
        List<TimeEntry> entries = timeEntryRepository.findAllByMissionIdAndWorkDateBetweenOrderByWorkDateAscIdAsc(
                missionId,
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
        );

        BigDecimal totalDayQuantity = sumQuantities(entries, TimeEntryUnitType.DAY);
        BigDecimal totalHalfDayQuantity = sumQuantities(entries, TimeEntryUnitType.HALF_DAY);
        BigDecimal totalHourQuantity = sumQuantities(entries, TimeEntryUnitType.HOUR);

        return new TimeEntrySummaryResponse(
                missionId,
                month,
                year,
                entries.size(),
                totalDayQuantity,
                totalHalfDayQuantity,
                totalHourQuantity
        );
    }

    public TimeEntry getTimeEntry(Long id) {
        return timeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeEntry", id));
    }

    private void validateListFilters(Long missionId, Long profileId) {
        if ((missionId == null && profileId == null) || (missionId != null && profileId != null)) {
            throw new IllegalArgumentException("Exactly one of missionId or profileId must be provided.");
        }
    }

    private void validateTimeEntryRelations(TimeEntry timeEntry) {
        if (timeEntry.getMission() == null) {
            throw new IllegalArgumentException("missionId cannot be null.");
        }
        if (timeEntry.getProfile() == null) {
            throw new IllegalArgumentException("profileId cannot be null.");
        }
        if (!timeEntry.getMission().getProfile().getId().equals(timeEntry.getProfile().getId())) {
            throw new IllegalArgumentException("profileId must match the profile assigned to the mission.");
        }

        LocalDate workDate = timeEntry.getWorkDate();
        if (workDate == null) {
            return;
        }

        if (timeEntry.getMission().getStartDate() != null && workDate.isBefore(timeEntry.getMission().getStartDate())) {
            throw new IllegalArgumentException("workDate must be greater than or equal to mission startDate.");
        }
        if (timeEntry.getMission().getEndDate() != null && workDate.isAfter(timeEntry.getMission().getEndDate())) {
            throw new IllegalArgumentException("workDate must be less than or equal to mission endDate.");
        }
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }

        String normalizedComment = comment.trim();
        return normalizedComment.isEmpty() ? null : normalizedComment;
    }

    private BigDecimal sumQuantities(List<TimeEntry> entries, TimeEntryUnitType unitType) {
        return entries.stream()
                .filter(entry -> entry.getUnitType() == unitType)
                .map(TimeEntry::getQuantity)
                .reduce(ZERO, BigDecimal::add);
    }
}
