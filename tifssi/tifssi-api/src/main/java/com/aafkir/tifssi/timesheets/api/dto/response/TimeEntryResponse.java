package com.aafkir.tifssi.timesheets.api.dto.response;

import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryStatus;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryUnitType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description = "Representation d'une saisie de temps.",
        example = """
                {
                  "id": 1,
                  "missionId": 1,
                  "profileId": 1,
                  "workDate": "2026-04-10",
                  "quantity": 0.50,
                  "unitType": "HALF_DAY",
                  "comment": "Updated after review",
                  "status": "VALIDATED",
                  "createdAt": "2026-04-07T09:15:30Z",
                  "updatedAt": "2026-04-07T10:00:00Z"
                }
                """
)
public record TimeEntryResponse(
        Long id,
        Long missionId,
        Long profileId,
        LocalDate workDate,
        BigDecimal quantity,
        TimeEntryUnitType unitType,
        String comment,
        @Schema(deprecated = true, description = "Ancien statut de saisie, ne représente pas la décision mensuelle. Consulter Timesheet.status.") TimeEntryStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
