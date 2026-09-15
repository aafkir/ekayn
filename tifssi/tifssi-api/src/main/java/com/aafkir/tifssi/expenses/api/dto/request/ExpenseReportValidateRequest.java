package com.aafkir.tifssi.expenses.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation d'une note de frais mensuelle")
public record ExpenseReportValidateRequest(Long managerId) {}
