package com.aafkir.tifssi.timesheets.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(
        description = "Synthese des saisies de temps sur une periode.",
        example = """
                {
                  "missionId": 1,
                  "month": 4,
                  "year": 2026,
                  "totalEntries": 1,
                  "totalDayQuantity": 0,
                  "totalHalfDayQuantity": 0.50,
                  "totalHourQuantity": 0
                }
                """
)
public record TimeEntrySummaryResponse(
        Long missionId,
        Integer month,
        Integer year,
        long totalEntries,
        BigDecimal totalDayQuantity,
        BigDecimal totalHalfDayQuantity,
        BigDecimal totalHourQuantity
) {
}
