package com.aafkir.tifssi.staffing.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
@NoArgsConstructor
@Schema(
        description = "Payload de mise a jour partielle d'un positionnement.",
        example = """
                {
                  "proposedDailyRate": 700.00,
                  "comment": "Updated after client call"
                }
                """
)
public class SubmissionPatchRequest {

    private JsonNullable<BigDecimal> proposedDailyRate = JsonNullable.undefined();
    private JsonNullable<String> comment = JsonNullable.undefined();
}
