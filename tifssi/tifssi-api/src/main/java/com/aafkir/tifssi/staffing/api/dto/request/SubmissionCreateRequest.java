package com.aafkir.tifssi.staffing.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(
        description = "Payload de creation d'un positionnement entre un besoin et un profil.",
        example = """
                {
                  "needId": 1,
                  "profileId": 1,
                  "proposedDailyRate": 650.00,
                  "comment": "First shortlist"
                }
                """
)
public record SubmissionCreateRequest(
        @NotNull Long needId,
        @NotNull Long profileId,
        @PositiveOrZero BigDecimal proposedDailyRate,
        @Size(max = 2000) String comment
) {
}
