package com.aafkir.tifssi.expenses.api.dto.request;

import com.aafkir.tifssi.expenses.domain.enums.ExpenseCategory;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(
        description = "Payload de creation d'une note de frais.",
        example = """
                {
                  "missionId": 1,
                  "profileId": 1,
                  "expenseDate": "2026-04-12",
                  "category": "TRAVEL",
                  "amount": 35.50,
                  "currency": "EUR",
                  "comment": "Train client",
                  "receiptUrl": "https://cdn.example.com/receipts/train.pdf",
                  "status": "SUBMITTED",
                  "billable": true
                }
                """
)
public record ExpenseCreateRequest(
        @NotNull Long missionId,
        @NotNull Long profileId,
        @NotNull LocalDate expenseDate,
        @NotNull ExpenseCategory category,
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @Size(max = 1000) String comment,
        @Size(max = 500) String receiptUrl,
        @NotNull ExpenseStatus status,
        @NotNull Boolean billable
) {
}
