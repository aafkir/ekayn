package com.aafkir.tifssi.expenses.api.dto.request;
import io.swagger.v3.oas.annotations.media.Schema; import jakarta.validation.constraints.*;
@Schema(description="Rejet d'une note de frais mensuelle") public record ExpenseReportRejectRequest(@NotBlank @Size(max=2000) String reason, Long managerId) {}
