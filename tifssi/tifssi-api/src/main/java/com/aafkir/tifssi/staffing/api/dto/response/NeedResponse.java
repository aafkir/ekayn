package com.aafkir.tifssi.staffing.api.dto.response;

import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description = "Representation d'un besoin de staffing.",
        example = """
                {
                  "id": 1,
                  "companyId": 1,
                  "contactId": 1,
                  "projectId": 12,
                  "needReference": "NEED-2026-001",
                  "title": "Consultant Java Senior",
                  "description": "Mission de build backend Spring Boot.",
                  "status": "WON",
                  "startDate": "2026-05-01",
                  "endDate": "2026-12-31",
                  "locationName": "Paris",
                  "remotePossible": true,
                  "targetDailyRate": 650.00,
                  "createdAt": "2026-04-07T09:15:30Z",
                  "updatedAt": "2026-04-07T09:15:30Z"
                }
                """
)
public record NeedResponse(
        Long id,
        Long companyId,
        Long contactId,
        Long projectId,
        String needReference,
        String title,
        String description,
        NeedStatus status,
        LocalDate startDate,
        LocalDate endDate,
        String locationName,
        boolean remotePossible,
        BigDecimal targetDailyRate,
        Instant createdAt,
        Instant updatedAt
) {
}
