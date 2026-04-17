package com.aafkir.tifssi.crm.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
@NoArgsConstructor
@Schema(
        description = "Payload de mise a jour partielle d'un contact.",
        example = """
                {
                  "jobTitle": "Directrice des achats groupe",
                  "phoneNumber": "+33605060708",
                  "primaryContact": false
                }
                """
)
public class ContactPatchRequest {

    private JsonNullable<Long> companyId = JsonNullable.undefined();
    private JsonNullable<String> firstName = JsonNullable.undefined();
    private JsonNullable<String> lastName = JsonNullable.undefined();
    private JsonNullable<String> jobTitle = JsonNullable.undefined();
    private JsonNullable<String> emailAddress = JsonNullable.undefined();
    private JsonNullable<String> phoneNumber = JsonNullable.undefined();
    private JsonNullable<Boolean> primaryContact = JsonNullable.undefined();
}
