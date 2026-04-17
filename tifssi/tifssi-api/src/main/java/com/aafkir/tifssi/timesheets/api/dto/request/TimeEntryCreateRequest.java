package com.aafkir.tifssi.timesheets.api.dto.request;

import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryStatus;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryUnitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(
        description = "Payload de creation d'une saisie de temps.",
        example = """
                {
                  "missionId": 1,
                  "profileId": 1,
                  "workDate": "2026-04-10",
                  "quantity": 1.00,
                  "unitType": "DAY",
                  "comment": "Sprint delivery",
                  "status": "DRAFT"
                }
                """
)
public record TimeEntryCreateRequest(
        @NotNull Long missionId,
        @NotNull Long profileId,
        @NotNull LocalDate workDate,
        @NotNull @Positive BigDecimal quantity,
        @NotNull TimeEntryUnitType unitType,
        @Size(max = 1000) String comment,
        @NotNull TimeEntryStatus status
) {
}
