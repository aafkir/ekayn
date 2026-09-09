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
                  "email": "nina.dupont@tifssi.example",
                  "phone": "+33611121314",
                  "role": "Developpeuse backend",
                  "seniority": "Senior",
                  "active": true,
                  "defaultDailyRate": 700.00,
                  "availabilityDate": "2026-04-15"
                }
                """
)
public record ProfileCreateRequest(
        @Schema(description = "Type de profil conforme au domaine staffing.", example = "INTERNAL")
        @NotNull
        ProfileType type,
        @Schema(description = "Prenom du profil.", example = "Nina")
        @NotBlank
        @Size(max = 100)
        String firstName,
        @Schema(description = "Nom du profil.", example = "Dupont")
        @NotBlank
        @Size(max = 100)
        String lastName,
        @Schema(description = "Adresse email du profil.", example = "nina.dupont@tifssi.example")
        @NotBlank
        @Email
        @Size(max = 150)
        String email,
        @Schema(description = "Numero de telephone du profil.", example = "+33611121314")
        @Size(max = 50)
        String phone,
        @Schema(description = "Role du profil. Mappe sur le champ metier jobTitle.", example = "Developpeuse backend")
        @Size(max = 150)
        String role,
        @Schema(description = "Niveau de seniorite du profil. Mappe sur le champ metier seniorityLabel.", example = "Senior")
        @Size(max = 100)
        String seniority,
        @Schema(description = "Indique si le profil est actif.", example = "true")
        @NotNull
        Boolean active,
        @Schema(description = "Taux journalier par defaut.", example = "700.00")
        @PositiveOrZero
        BigDecimal defaultDailyRate,
        @Schema(description = "Date de disponibilite du profil.", example = "2026-04-15")
        LocalDate availabilityDate
) {
}
