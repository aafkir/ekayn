package com.aafkir.tifssi.staffing.api.dto.response;

import com.aafkir.tifssi.staffing.domain.enums.SubmissionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description = "Representation d'un positionnement entre un besoin et un profil.",
        example = """
                {
                  "id": 1,
                  "needId": 1,
                  "profileId": 1,
                  "status": "SENT",
                  "submittedAt": "2026-04-07T09:30:00Z",
                  "proposedDailyRate": 700.00,
                  "comment": "Updated after client call",
                  "createdAt": "2026-04-07T09:15:30Z",
                  "updatedAt": "2026-04-07T09:30:00Z"
                }
                """
)
public record SubmissionResponse(
        Long id,
        Long needId,
        Long profileId,
        SubmissionStatus status,
        Instant submittedAt,
        BigDecimal proposedDailyRate,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {
}
