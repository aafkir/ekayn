package com.aafkir.tifssi.absences.api.dto.request;

import com.aafkir.tifssi.absences.domain.enums.AbsenceStatus;
import com.aafkir.tifssi.absences.domain.enums.AbsenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(
        description = "Payload de creation d'une absence collaborateur.",
        example = """
                {
                  "profileId": 1,
                  "type": "PAID_LEAVE",
                  "startDate": "2026-07-10",
                  "endDate": "2026-07-12",
                  "quantity": 3.00,
                  "comment": "Vacances ete",
                  "status": "SUBMITTED"
                }
                """
)
public record AbsenceCreateRequest(
        @NotNull Long profileId,
        @NotNull AbsenceType type,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull @Positive BigDecimal quantity,
        @Size(max = 1000) String comment,
        @NotNull AbsenceStatus status
) {
}
