package com.aafkir.tifssi.staffing.api.dto.request;

import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
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
        description = "Payload de mise a jour partielle d'un besoin.",
        example = """
                {
                  "status": "WON",
                  "targetDailyRate": 700.00,
                  "locationName": "Paris La Defense"
                }
                """
)
public class NeedPatchRequest {

    private JsonNullable<Long> companyId = JsonNullable.undefined();
    private JsonNullable<Long> contactId = JsonNullable.undefined();
    private JsonNullable<String> needReference = JsonNullable.undefined();
    private JsonNullable<String> title = JsonNullable.undefined();
    private JsonNullable<String> description = JsonNullable.undefined();
    private JsonNullable<NeedStatus> status = JsonNullable.undefined();
    private JsonNullable<LocalDate> startDate = JsonNullable.undefined();
    private JsonNullable<LocalDate> endDate = JsonNullable.undefined();
    private JsonNullable<String> locationName = JsonNullable.undefined();
    private JsonNullable<Boolean> remotePossible = JsonNullable.undefined();
    private JsonNullable<BigDecimal> targetDailyRate = JsonNullable.undefined();
}
