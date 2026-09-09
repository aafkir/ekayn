package com.aafkir.tifssi.staffing.api.dto.request;

import com.aafkir.tifssi.staffing.domain.enums.SubmissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(
        description = "Payload de creation d'un positionnement pour un besoin donne.",
        example = """
                {
                  "profileId": 1,
                  "proposedDailyRate": 650.00,
                  "status": "PRESELECTED",
                  "comment": "First shortlist"
                }
                """
)
public record NeedSubmissionCreateRequest(
        @NotNull Long profileId,
        @PositiveOrZero BigDecimal proposedDailyRate,
        @NotNull SubmissionStatus status,
        @Size(max = 2000) String comment
) {
}
