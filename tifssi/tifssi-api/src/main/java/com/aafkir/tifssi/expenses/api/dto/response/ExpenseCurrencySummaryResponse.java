package com.aafkir.tifssi.expenses.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(
        description = "Synthese des montants de frais pour une devise.",
        example = """
                {
                  "currency": "USD",
                  "totalAmount": 45.00,
                  "billableAmount": 0,
                  "nonBillableAmount": 45.00
                }
                """
)
public record ExpenseCurrencySummaryResponse(
        String currency,
        BigDecimal totalAmount,
        BigDecimal billableAmount,
        BigDecimal nonBillableAmount
) {
}
