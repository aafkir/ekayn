package com.aafkir.tifssi.timesheets.api.dto.request;
import jakarta.validation.constraints.NotBlank;
public record TimesheetRejectRequest(@NotBlank String reason, Long managerId) {}
