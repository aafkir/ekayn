package com.aafkir.tifssi.staffing.api.dto.request;

import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(
        description = "Payload de creation d'un profil collaborateur ou consultant.",
        example = """
                {
                  "type": "INTERNAL",
                  "firstName": "Nina",
                  "lastName": "Dupont",
                  "emailAddress": "nina.dupont@tifssi.example",
                  "phoneNumber": "+33611121314",
                  "jobTitle": "Developpeuse backend",
                  "seniorityLabel": "Senior",
                  "active": true,
                  "defaultDailyRate": 700.00,
                  "availabilityDate": "2026-04-15"
                }
                """
)
public record ProfileCreateRequest(
        @NotNull ProfileType type,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 150) String emailAddress,
        @Size(max = 50) String phoneNumber,
        @Size(max = 150) String jobTitle,
        @Size(max = 100) String seniorityLabel,
        @NotNull Boolean active,
        @PositiveOrZero BigDecimal defaultDailyRate,
        LocalDate availabilityDate
) {
}
