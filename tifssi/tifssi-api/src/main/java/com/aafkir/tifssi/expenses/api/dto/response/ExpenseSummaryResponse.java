package com.aafkir.tifssi.expenses.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
        description = "Synthese des notes de frais sur une periode.",
        example = """
                {
                  "missionId": 1,
                  "month": 4,
                  "year": 2026,
                  "totalExpenses": 1,
                  "billableExpenses": 0,
                  "nonBillableExpenses": 1,
                  "totalsByCurrency": [
                    {
                      "currency": "USD",
                      "totalAmount": 45.00,
                      "billableAmount": 0,
                      "nonBillableAmount": 45.00
                    }
                  ]
                }
                """
)
public record ExpenseSummaryResponse(
        Long missionId,
        Integer month,
        Integer year,
        long totalExpenses,
        long billableExpenses,
        long nonBillableExpenses,
        List<ExpenseCurrencySummaryResponse> totalsByCurrency
) {
}
