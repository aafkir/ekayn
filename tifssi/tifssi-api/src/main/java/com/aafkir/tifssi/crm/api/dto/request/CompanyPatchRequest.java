package com.aafkir.tifssi.crm.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
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

    private JsonNullable<String> name = JsonNullable.undefined();
    private JsonNullable<String> legalName = JsonNullable.undefined();
    private JsonNullable<String> displayName = JsonNullable.undefined();
    private JsonNullable<String> relationType = JsonNullable.undefined();
    private JsonNullable<String> status = JsonNullable.undefined();
    private JsonNullable<String> sector = JsonNullable.undefined();
    private JsonNullable<String> manager = JsonNullable.undefined();
    private JsonNullable<String> managerName = JsonNullable.undefined();
    private JsonNullable<String> agency = JsonNullable.undefined();
    private JsonNullable<String> currentAction = JsonNullable.undefined();
    private JsonNullable<String> nextAction = JsonNullable.undefined();
    private JsonNullable<LocalDate> actionDate = JsonNullable.undefined();
    private JsonNullable<LocalDate> nextActionDate = JsonNullable.undefined();
    private JsonNullable<String> registrationNumber = JsonNullable.undefined();
    private JsonNullable<String> siret = JsonNullable.undefined();
    private JsonNullable<String> vatNumber = JsonNullable.undefined();
    private JsonNullable<String> websiteUrl = JsonNullable.undefined();
    private JsonNullable<String> website = JsonNullable.undefined();
    private JsonNullable<String> emailAddress = JsonNullable.undefined();
    private JsonNullable<String> email = JsonNullable.undefined();
    private JsonNullable<String> phoneNumber = JsonNullable.undefined();
    private JsonNullable<String> phone = JsonNullable.undefined();
    private JsonNullable<String> billingAddress = JsonNullable.undefined();
    private JsonNullable<String> address = JsonNullable.undefined();
    private JsonNullable<String> cityName = JsonNullable.undefined();
    private JsonNullable<String> city = JsonNullable.undefined();
    private JsonNullable<String> postalCode = JsonNullable.undefined();
    private JsonNullable<String> countryCode = JsonNullable.undefined();
    private JsonNullable<String> country = JsonNullable.undefined();
}
