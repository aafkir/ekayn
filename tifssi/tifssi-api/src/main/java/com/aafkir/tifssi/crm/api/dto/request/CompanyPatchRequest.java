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
        description = "Payload de mise a jour partielle d'une entreprise.",
        example = """
                {
                  "displayName": "Acme Groupe",
                  "websiteUrl": "https://group.acme.example",
                  "cityName": "Lyon"
                }
                """
)
public class CompanyPatchRequest {

    private JsonNullable<String> legalName = JsonNullable.undefined();
    private JsonNullable<String> displayName = JsonNullable.undefined();
    private JsonNullable<String> registrationNumber = JsonNullable.undefined();
    private JsonNullable<String> vatNumber = JsonNullable.undefined();
    private JsonNullable<String> websiteUrl = JsonNullable.undefined();
    private JsonNullable<String> emailAddress = JsonNullable.undefined();
    private JsonNullable<String> phoneNumber = JsonNullable.undefined();
    private JsonNullable<String> billingAddress = JsonNullable.undefined();
    private JsonNullable<String> cityName = JsonNullable.undefined();
    private JsonNullable<String> postalCode = JsonNullable.undefined();
    private JsonNullable<String> countryCode = JsonNullable.undefined();
}
