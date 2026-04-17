package com.aafkir.tifssi.staffing.api.dto.request;

import com.aafkir.tifssi.staffing.domain.enums.SubmissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        description = "Payload de mise a jour du statut d'un positionnement.",
        example = """
                {
                  "status": "SENT"
                }
                """
)
public record SubmissionStatusPatchRequest(
        @NotNull SubmissionStatus status
) {
}
