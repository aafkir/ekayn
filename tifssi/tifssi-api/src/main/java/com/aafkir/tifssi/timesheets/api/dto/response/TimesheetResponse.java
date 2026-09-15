package com.aafkir.tifssi.timesheets.api.dto.response;

import com.aafkir.tifssi.timesheets.domain.enums.TimesheetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Feuille CRA mensuelle unique par profileId, year et month. Le statut concerne toute la feuille et ne dépend jamais des absences.")
public record TimesheetResponse(
        Long id,
        Long profileId,
        Integer year,
        Integer month,
        TimesheetStatus status,
        Instant submittedAt,
        Instant validatedAt,
        Long validatedBy,
        Instant rejectedAt,
        Long rejectedBy,
        String rejectionReason,
        List<TimeEntryResponse> timeEntries,
        Instant createdAt,
        Instant updatedAt
) {}
