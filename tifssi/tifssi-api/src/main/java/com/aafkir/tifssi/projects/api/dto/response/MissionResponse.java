package com.aafkir.tifssi.projects.api.dto.response;

import com.aafkir.tifssi.projects.domain.enums.MissionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description = "Representation d'une mission projet.",
        example = """
                {
                  "id": 1,
                  "projectId": 1,
                  "profileId": 1,
                  "roleName": "Lead Backend",
                  "startDate": "2026-05-01",
                  "endDate": "2026-10-31",
                  "dailyRate": 800.00,
                  "allocationPercent": 80,
                  "status": "ACTIVE",
                  "createdAt": "2026-04-07T09:15:30Z",
                  "updatedAt": "2026-04-07T09:15:30Z"
                }
                """
)
public record MissionResponse(
        Long id,
        Long projectId,
        Long profileId,
        String roleName,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal dailyRate,
        Integer allocationPercent,
        MissionStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
