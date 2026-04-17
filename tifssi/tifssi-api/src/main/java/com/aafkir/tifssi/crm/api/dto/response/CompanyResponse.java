package com.aafkir.tifssi.crm.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description = "Representation d'une entreprise cliente.",
        example = """
                {
                  "id": 1,
                  "legalName": "Acme Conseil",
                  "displayName": "Acme",
                  "registrationNumber": "RCS-123456",
                  "vatNumber": "FR12345678901",
                  "websiteUrl": "https://acme.example",
                  "emailAddress": "contact@acme.example",
                  "phoneNumber": "+33102030405",
                  "billingAddress": "12 rue de Paris",
                  "cityName": "Paris",
                  "postalCode": "75001",
                  "countryCode": "FR",
                  "createdAt": "2026-04-07T09:15:30Z",
                  "updatedAt": "2026-04-07T09:15:30Z"
                }
                """
)
public record CompanyResponse(
        Long id,
        String legalName,
        String displayName,
        String registrationNumber,
        String vatNumber,
        String websiteUrl,
        String emailAddress,
        String phoneNumber,
        String billingAddress,
        String cityName,
        String postalCode,
        String countryCode,
        Instant createdAt,
        Instant updatedAt
) {
}
