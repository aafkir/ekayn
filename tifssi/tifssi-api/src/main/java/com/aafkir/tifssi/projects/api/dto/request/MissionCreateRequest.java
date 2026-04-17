package com.aafkir.tifssi.projects.api.dto.request;

import com.aafkir.tifssi.projects.domain.enums.MissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(
        description = "Payload de creation d'une mission rattachee a un projet et un profil.",
        example = """
                {
                  "projectId": 1,
                  "profileId": 1,
                  "roleName": "Lead Backend",
                  "startDate": "2026-05-01",
                  "endDate": "2026-10-31",
                  "dailyRate": 750.00,
                  "allocationPercent": 100,
                  "status": "ACTIVE"
                }
                """
)
public record MissionCreateRequest(
        @NotNull Long projectId,
        @NotNull Long profileId,
        @NotBlank @Size(max = 150) String roleName,
        LocalDate startDate,
        LocalDate endDate,
        @PositiveOrZero BigDecimal dailyRate,
        @Min(0) @Max(100) Integer allocationPercent,
        @NotNull MissionStatus status
) {
}
