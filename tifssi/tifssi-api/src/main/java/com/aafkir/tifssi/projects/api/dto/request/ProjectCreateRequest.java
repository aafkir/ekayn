package com.aafkir.tifssi.projects.api.dto.request;

import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(
        description = "Payload de creation d'un projet.",
        example = """
                {
                  "companyId": 1,
                  "originNeedId": 1,
                  "contactId": 1,
                  "projectCode": "PRJ-2026-001",
                  "projectName": "Plateforme staffing",
                  "description": "Projet gagne suite au besoin NEED-2026-001.",
                  "status": "ACTIVE",
                  "startDate": "2026-05-01",
                  "endDate": "2026-12-31",
                  "budgetAmount": 120000.00
                }
                """
)
public record ProjectCreateRequest(
        @NotNull Long companyId,
        Long originNeedId,
        Long contactId,
        @NotBlank @Size(max = 50) String projectCode,
        @NotBlank @Size(max = 150) String projectName,
        @Size(max = 4000) String description,
        @NotNull ProjectStatus status,
        LocalDate startDate,
        LocalDate endDate,
        @PositiveOrZero BigDecimal budgetAmount
) {
}
