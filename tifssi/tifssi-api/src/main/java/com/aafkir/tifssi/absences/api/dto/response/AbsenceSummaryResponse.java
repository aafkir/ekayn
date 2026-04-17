package com.aafkir.tifssi.absences.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(
        description = "Synthese annuelle des absences d'un profil.",
        example = """
                {
                  "profileId": 1,
                  "year": 2026,
                  "totalAbsences": 2,
                  "totalQuantity": 3.50
                }
                """
)
public record AbsenceSummaryResponse(
        Long profileId,
        Integer year,
        long totalAbsences,
        BigDecimal totalQuantity
) {
}
