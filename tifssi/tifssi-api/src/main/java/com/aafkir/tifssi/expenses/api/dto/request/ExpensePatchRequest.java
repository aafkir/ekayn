package com.aafkir.tifssi.expenses.api.dto.request;

import com.aafkir.tifssi.expenses.domain.enums.ExpenseCategory;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
@NoArgsConstructor
@Schema(
        description = "Payload de mise a jour partielle d'une note de frais.",
        example = """
                {
                  "category": "MEAL",
                  "amount": 45.00,
                  "currency": "USD",
                  "comment": "Updated meal",
                  "receiptUrl": "https://cdn.example.com/meal.pdf",
                  "status": "VALIDATED",
                  "billable": false
                }
                """
)
public class ExpensePatchRequest {

    private JsonNullable<Long> missionId = JsonNullable.undefined();
    private JsonNullable<Long> profileId = JsonNullable.undefined();
    private JsonNullable<LocalDate> expenseDate = JsonNullable.undefined();
    private JsonNullable<ExpenseCategory> category = JsonNullable.undefined();
    private JsonNullable<BigDecimal> amount = JsonNullable.undefined();
    private JsonNullable<String> currency = JsonNullable.undefined();
    private JsonNullable<String> comment = JsonNullable.undefined();
    private JsonNullable<String> receiptUrl = JsonNullable.undefined();
    private JsonNullable<ExpenseStatus> status = JsonNullable.undefined();
    private JsonNullable<Boolean> billable = JsonNullable.undefined();
}
