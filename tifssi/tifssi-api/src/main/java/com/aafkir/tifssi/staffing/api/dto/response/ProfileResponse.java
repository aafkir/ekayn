package com.aafkir.tifssi.staffing.api.dto.response;

import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description = "Representation d'un profil staffing.",
        example = """
                {
                  "id": 1,
                  "type": "INTERNAL",
                  "firstName": "Nina",
                  "lastName": "Dupont",
                  "emailAddress": "nina.dupont@tifssi.example",
                  "phoneNumber": "+33611121314",
                  "jobTitle": "Lead Backend",
                  "seniorityLabel": "Expert",
                  "active": true,
                  "defaultDailyRate": 750.00,
                  "availabilityDate": "2026-04-22",
                  "createdAt": "2026-04-07T09:15:30Z",
                  "updatedAt": "2026-04-07T09:15:30Z"
                }
                """
)
public record ProfileResponse(
        Long id,
        ProfileType type,
        String firstName,
        String lastName,
        String emailAddress,
        String phoneNumber,
        String jobTitle,
        String seniorityLabel,
        boolean active,
        BigDecimal defaultDailyRate,
        LocalDate availabilityDate,
        Instant createdAt,
        Instant updatedAt
) {
}
