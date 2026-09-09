package com.aafkir.tifssi.timesheets.api.dto.response;
import com.aafkir.tifssi.timesheets.domain.enums.TimesheetStatus;
import java.time.Instant; import java.util.List;
public record TimesheetResponse(Long id, Long profileId, Integer year, Integer month, TimesheetStatus status, Instant submittedAt, Instant validatedAt, Long validatedBy, Instant rejectedAt, Long rejectedBy, String rejectionReason, List<TimeEntryResponse> timeEntries, Instant createdAt, Instant updatedAt) {}
