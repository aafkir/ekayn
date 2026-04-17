package com.aafkir.tifssi.projects.api.dto.response;

import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description = "Representation d'un projet.",
        example = """
                {
                  "id": 1,
                  "companyId": 1,
                  "originNeedId": 1,
                  "contactId": 1,
                  "projectCode": "PRJ-2026-001",
                  "projectName": "Plateforme staffing",
                  "description": "Projet gagne suite au besoin NEED-2026-001.",
                  "status": "ACTIVE",
                  "startDate": "2026-05-01",
                  "endDate": "2026-12-31",
                  "budgetAmount": 120000.00,
                  "createdAt": "2026-04-07T09:15:30Z",
                  "updatedAt": "2026-04-07T09:15:30Z"
                }
                """
)
public record ProjectResponse(
        Long id,
        Long companyId,
        Long originNeedId,
        Long contactId,
        String projectCode,
        String projectName,
        String description,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal budgetAmount,
        Instant createdAt,
        Instant updatedAt
) {
}
