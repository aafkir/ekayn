package com.aafkir.tifssi.timesheets.api.dto.request;

import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryStatus;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryUnitType;
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
        description = "Payload de mise a jour partielle d'une saisie de temps.",
        example = """
                {
                  "quantity": 0.50,
                  "unitType": "HALF_DAY",
                  "comment": "Updated after review",
                  "status": "VALIDATED"
                }
                """
)
public class TimeEntryPatchRequest {

    private JsonNullable<Long> missionId = JsonNullable.undefined();
    private JsonNullable<Long> profileId = JsonNullable.undefined();
    private JsonNullable<LocalDate> workDate = JsonNullable.undefined();
    private JsonNullable<BigDecimal> quantity = JsonNullable.undefined();
    private JsonNullable<TimeEntryUnitType> unitType = JsonNullable.undefined();
    private JsonNullable<String> comment = JsonNullable.undefined();
    private JsonNullable<TimeEntryStatus> status = JsonNullable.undefined();
}
