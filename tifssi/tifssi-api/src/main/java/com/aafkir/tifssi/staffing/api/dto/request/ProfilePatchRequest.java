package com.aafkir.tifssi.staffing.api.dto.request;

import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import io.swagger.v3.oas.annotations.media.Schema;
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
                  "jobTitle": "Lead Backend",
                  "seniorityLabel": "Expert",
                  "defaultDailyRate": 750.00,
                  "availabilityDate": "2026-04-22"
                }
                """
)
public class ProfilePatchRequest {

    private JsonNullable<ProfileType> type = JsonNullable.undefined();
    private JsonNullable<String> firstName = JsonNullable.undefined();
    private JsonNullable<String> lastName = JsonNullable.undefined();
    private JsonNullable<String> emailAddress = JsonNullable.undefined();
    private JsonNullable<String> phoneNumber = JsonNullable.undefined();
    private JsonNullable<String> jobTitle = JsonNullable.undefined();
    private JsonNullable<String> seniorityLabel = JsonNullable.undefined();
    private JsonNullable<Boolean> active = JsonNullable.undefined();
    private JsonNullable<java.math.BigDecimal> defaultDailyRate = JsonNullable.undefined();
    private JsonNullable<java.time.LocalDate> availabilityDate = JsonNullable.undefined();
}
