package com.aafkir.tifssi.expenses.api.dto.response;

import com.aafkir.tifssi.expenses.domain.enums.ExpenseReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal; import java.time.Instant; import java.util.List;

@Schema(description="Note de frais mensuelle unique par profil, année et mois.")
public record ExpenseReportResponse(Long id, Long profileId, Integer year, Integer month,
        ExpenseReportStatus status, BigDecimal totalAmount, String currency,
        Instant submittedAt, Instant validatedAt, Long validatedBy, Instant rejectedAt,
        Long rejectedBy, String rejectionReason, List<ExpenseResponse> expenses,
        Instant createdAt, Instant updatedAt) {}
