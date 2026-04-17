package com.aafkir.tifssi.projects.api.dto.request;

import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
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
        description = "Payload de mise a jour partielle d'un projet.",
        example = """
                {
                  "status": "COMPLETED",
                  "endDate": "2026-11-30",
                  "budgetAmount": 128500.00
                }
                """
)
public class ProjectPatchRequest {

    private JsonNullable<Long> companyId = JsonNullable.undefined();
    private JsonNullable<Long> originNeedId = JsonNullable.undefined();
    private JsonNullable<Long> contactId = JsonNullable.undefined();
    private JsonNullable<String> projectCode = JsonNullable.undefined();
    private JsonNullable<String> projectName = JsonNullable.undefined();
    private JsonNullable<String> description = JsonNullable.undefined();
    private JsonNullable<ProjectStatus> status = JsonNullable.undefined();
    private JsonNullable<LocalDate> startDate = JsonNullable.undefined();
    private JsonNullable<LocalDate> endDate = JsonNullable.undefined();
    private JsonNullable<BigDecimal> budgetAmount = JsonNullable.undefined();
}
