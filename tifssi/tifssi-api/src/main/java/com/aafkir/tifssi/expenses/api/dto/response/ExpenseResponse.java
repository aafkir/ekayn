package com.aafkir.tifssi.expenses.api.dto.response;

import com.aafkir.tifssi.expenses.domain.enums.ExpenseCategory;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description = "Representation d'une note de frais.",
        example = """
                {
                  "id": 1,
                  "missionId": 1,
                  "profileId": 1,
                  "expenseDate": "2026-04-12",
                  "category": "MEAL",
                  "amount": 45.00,
                  "currency": "USD",
                  "comment": "Updated meal",
                  "receiptUrl": "https://cdn.example.com/meal.pdf",
                  "status": "VALIDATED",
                  "billable": false,
                  "createdAt": "2026-04-07T09:15:30Z",
                  "updatedAt": "2026-04-07T10:00:00Z"
                }
                """
)
public record ExpenseResponse(
        Long id,
        Long missionId,
        Long profileId,
        LocalDate expenseDate,
        ExpenseCategory category,
        BigDecimal amount,
        String currency,
        String comment,
        String receiptUrl,
        ExpenseStatus status,
        boolean billable,
        Instant createdAt,
        Instant updatedAt
) {
}
