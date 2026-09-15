package com.aafkir.tifssi.timesheets.application.service;

import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.staffing.application.service.ProfileService;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import com.aafkir.tifssi.timesheets.api.dto.request.TimesheetRejectRequest;
import com.aafkir.tifssi.timesheets.api.dto.response.TimesheetResponse;
import com.aafkir.tifssi.timesheets.api.mapper.TimesheetApiMapper;
import com.aafkir.tifssi.timesheets.domain.enums.TimesheetStatus;
import com.aafkir.tifssi.timesheets.domain.model.Timesheet;
import com.aafkir.tifssi.timesheets.application.exception.TimesheetStateException;
import com.aafkir.tifssi.timesheets.infrastructure.repository.TimesheetRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TimesheetService {
    private final TimesheetRepository repository;
    private final TimesheetApiMapper mapper;
    private final ProfileService profileService;
    private final ProfileRepository profiles;

    public TimesheetService(TimesheetRepository repository, TimesheetApiMapper mapper,
                            ProfileService profileService, ProfileRepository profiles) {
        this.repository = repository;
        this.mapper = mapper;
        this.profileService = profileService;
        this.profiles = profiles;
    }

    @Transactional(readOnly = true)
    public List<TimesheetResponse> findAll(Long profileId, Integer year, Integer month) {
        if (year != null && year < 2000) throw new IllegalArgumentException("year must be at least 2000.");
        if (month != null && (month < 1 || month > 12)) throw new IllegalArgumentException("month must be between 1 and 12.");
        if (profileId != null) profileService.getProfile(profileId);
        return repository.findAllByFilters(profileId, year, month).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TimesheetResponse get(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    // The profile lock serializes creation of the first sheet for a given month.
    // The sheet version also detects concurrent edits versus submission/review.
    public Timesheet editableFor(Profile profile, LocalDate date) {
        if (date.getYear() < 2000) throw new IllegalArgumentException("Timesheet year must be at least 2000.");
        profiles.findForTimesheetCreation(profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile", profile.getId()));
        Timesheet sheet = repository.findByProfileIdAndYearAndMonth(profile.getId(), date.getYear(), date.getMonthValue())
                .orElseGet(() -> {
                    Timesheet created = new Timesheet();
                    created.setProfile(profile);
                    created.setYear(date.getYear());
                    created.setMonth(date.getMonthValue());
                    return repository.save(created);
                });
        requireEditable(sheet);
        return sheet;
    }

    public void requireEditable(Timesheet sheet) {
        if (sheet == null) throw new TimesheetStateException("TimeEntry must belong to a monthly timesheet.");
        if (sheet.getStatus() != TimesheetStatus.DRAFT && sheet.getStatus() != TimesheetStatus.REJECTED) {
            throw new TimesheetStateException("Only DRAFT or REJECTED timesheets can be edited.");
        }
        sheet.setUpdatedAt(Instant.now());
    }

    public TimesheetResponse submit(Long id) {
        Timesheet sheet = getEntity(id);
        requireEditable(sheet);
        if (sheet.getTimeEntries().isEmpty()) throw new TimesheetStateException("An empty timesheet cannot be submitted.");
        sheet.setStatus(TimesheetStatus.SUBMITTED);
        sheet.setSubmittedAt(Instant.now());
        sheet.setValidatedAt(null);
        sheet.setValidatedBy(null);
        sheet.setRejectedAt(null);
        sheet.setRejectedBy(null);
        sheet.setRejectionReason(null);
        return mapper.toResponse(sheet);
    }

    public TimesheetResponse validate(Long id, Long managerId) {
        Timesheet sheet = submitted(id);
        sheet.setStatus(TimesheetStatus.VALIDATED);
        sheet.setValidatedAt(Instant.now());
        sheet.setValidatedBy(managerId);
        return mapper.toResponse(sheet);
    }

    public TimesheetResponse reject(Long id, TimesheetRejectRequest request) {
        if (request == null || request.reason() == null || request.reason().isBlank() || request.reason().length() > 2000) {
            throw new IllegalArgumentException("A rejection reason of 1 to 2000 characters is required.");
        }
        Timesheet sheet = submitted(id);
        sheet.setStatus(TimesheetStatus.REJECTED);
        sheet.setRejectedAt(Instant.now());
        sheet.setRejectedBy(request.managerId());
        sheet.setRejectionReason(request.reason().trim());
        return mapper.toResponse(sheet);
    }

    private Timesheet submitted(Long id) {
        Timesheet sheet = getEntity(id);
        if (sheet.getStatus() != TimesheetStatus.SUBMITTED) {
            throw new TimesheetStateException("Only SUBMITTED timesheets can be validated or rejected.");
        }
        return sheet;
    }

    public Timesheet getEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Timesheet", id));
    }
}
