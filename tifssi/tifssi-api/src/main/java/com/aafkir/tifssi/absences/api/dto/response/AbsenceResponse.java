package com.aafkir.tifssi.absences.api.dto.response;

import com.aafkir.tifssi.absences.domain.enums.AbsenceStatus;
import com.aafkir.tifssi.absences.domain.enums.AbsenceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description = "Representation d'une absence collaborateur.",
        example = """
                {
                  "id": 1,
                  "profileId": 1,
                  "type": "PAID_LEAVE",
                  "startDate": "2026-07-10",
                  "endDate": "2026-07-12",
                  "quantity": 2.50,
                  "comment": "Ajuste apres validation RH",
                  "status": "APPROVED",
                  "createdAt": "2026-04-07T09:15:30Z",
                  "updatedAt": "2026-04-07T10:00:00Z"
                }
                """
)
public record AbsenceResponse(
        Long id,
        Long profileId,
        AbsenceType type,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal quantity,
        String comment,
        AbsenceStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
