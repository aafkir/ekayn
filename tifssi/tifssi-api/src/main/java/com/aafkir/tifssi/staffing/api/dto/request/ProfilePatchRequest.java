package com.aafkir.tifssi.staffing.api.dto.request;

import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
@NoArgsConstructor
@Schema(
                description = "Payload de mise a jour partielle d'un profil.",
                example = """
                {
                  "role": "Lead Backend",
                  "seniority": "Expert",
                  "defaultDailyRate": 750.00,
                  "availabilityDate": "2026-04-22"
                }
                """
)
public class ProfilePatchRequest {

    @Schema(description = "Type de profil conforme au domaine staffing.", example = "EXTERNAL")
    private JsonNullable<ProfileType> type = JsonNullable.undefined();
    @Schema(description = "Prenom du profil.", example = "Nina", maxLength = 100)
    private JsonNullable<String> firstName = JsonNullable.undefined();
    @Schema(description = "Nom du profil.", example = "Dupont", maxLength = 100)
    private JsonNullable<String> lastName = JsonNullable.undefined();
    @Schema(description = "Adresse email du profil.", example = "nina.dupont@tifssi.example", maxLength = 150)
    private JsonNullable<String> email = JsonNullable.undefined();
    @Schema(description = "Numero de telephone du profil.", example = "+33611121314", maxLength = 50)
    private JsonNullable<String> phone = JsonNullable.undefined();
    @Schema(description = "Role du profil. Mappe sur le champ metier jobTitle.", example = "Lead Backend", maxLength = 150)
    private JsonNullable<String> role = JsonNullable.undefined();
    @Schema(description = "Niveau de seniorite du profil. Mappe sur le champ metier seniorityLabel.", example = "Expert", maxLength = 100)
    private JsonNullable<String> seniority = JsonNullable.undefined();
    @Schema(description = "Indique si le profil est actif.", example = "true")
    private JsonNullable<Boolean> active = JsonNullable.undefined();
    @Schema(description = "Taux journalier par defaut. Doit etre positif ou nul.", example = "750.00")
    private JsonNullable<BigDecimal> defaultDailyRate = JsonNullable.undefined();
    @Schema(description = "Date de disponibilite du profil.", example = "2026-04-22")
    private JsonNullable<LocalDate> availabilityDate = JsonNullable.undefined();
}
