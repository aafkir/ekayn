package com.aafkir.tifssi.projects.api.dto.request;

import com.aafkir.tifssi.projects.domain.enums.MissionStatus;
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
        description = "Payload de mise a jour partielle d'une mission.",
        example = """
                {
                  "dailyRate": 800.00,
                  "allocationPercent": 80,
                  "status": "COMPLETED"
                }
                """
)
public class MissionPatchRequest {

    private JsonNullable<Long> projectId = JsonNullable.undefined();
    private JsonNullable<Long> profileId = JsonNullable.undefined();
    private JsonNullable<String> roleName = JsonNullable.undefined();
    private JsonNullable<LocalDate> startDate = JsonNullable.undefined();
    private JsonNullable<LocalDate> endDate = JsonNullable.undefined();
    private JsonNullable<BigDecimal> dailyRate = JsonNullable.undefined();
    private JsonNullable<Integer> allocationPercent = JsonNullable.undefined();
    private JsonNullable<MissionStatus> status = JsonNullable.undefined();
}
