package com.aafkir.tifssi.absences.api.dto.request;

import com.aafkir.tifssi.absences.domain.enums.AbsenceStatus;
import com.aafkir.tifssi.absences.domain.enums.AbsenceType;
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
        description = "Payload de mise a jour partielle d'une absence.",
        example = """
                {
                  "quantity": 2.50,
                  "comment": "Ajuste apres validation RH",
                  "status": "APPROVED"
                }
                """
)
public class AbsencePatchRequest {

    private JsonNullable<Long> profileId = JsonNullable.undefined();
    private JsonNullable<AbsenceType> type = JsonNullable.undefined();
    private JsonNullable<LocalDate> startDate = JsonNullable.undefined();
    private JsonNullable<LocalDate> endDate = JsonNullable.undefined();
    private JsonNullable<BigDecimal> quantity = JsonNullable.undefined();
    private JsonNullable<String> comment = JsonNullable.undefined();
    private JsonNullable<AbsenceStatus> status = JsonNullable.undefined();
}
