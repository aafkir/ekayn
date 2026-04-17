package com.aafkir.tifssi.staffing.api.dto.request;

import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(
        description = "Payload de creation d'un besoin de staffing.",
        example = """
                {
                  "companyId": 1,
                  "contactId": 1,
                  "needReference": "NEED-2026-001",
                  "title": "Consultant Java Senior",
                  "description": "Mission de build backend Spring Boot.",
                  "status": "OPEN",
                  "startDate": "2026-05-01",
                  "endDate": "2026-12-31",
                  "locationName": "Paris",
                  "remotePossible": true,
                  "targetDailyRate": 650.00
                }
                """
)
public record NeedCreateRequest(
        @NotNull Long companyId,
        Long contactId,
        @Size(max = 50) String needReference,
        @NotBlank @Size(max = 150) String title,
        @Size(max = 4000) String description,
        @NotNull NeedStatus status,
        LocalDate startDate,
        LocalDate endDate,
        @Size(max = 150) String locationName,
        @NotNull Boolean remotePossible,
        @PositiveOrZero BigDecimal targetDailyRate
) {
}
