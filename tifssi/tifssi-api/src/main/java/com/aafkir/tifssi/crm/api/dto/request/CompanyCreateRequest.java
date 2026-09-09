package com.aafkir.tifssi.crm.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(
        description = "Payload de creation d'une entreprise cliente.",
        example = """
                {
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
                  "countryCode": "FR"
                }
                """
)
public record CompanyCreateRequest(
        @NotBlank @Size(max = 150) String legalName,
        @Size(max = 150) String displayName,
        @Size(max = 30) String relationType,
        @Size(max = 30) String status,
        @Size(max = 100) String sector,
        @Size(max = 150) String managerName,
        @Size(max = 100) String agency,
        @Size(max = 150) String currentAction,
        LocalDate actionDate,
        @Size(max = 50) String registrationNumber,
        @Size(max = 50) String vatNumber,
        @Size(max = 255) String websiteUrl,
        @Email @Size(max = 150) String emailAddress,
        @Size(max = 50) String phoneNumber,
        @Size(max = 255) String billingAddress,
        @Size(max = 100) String cityName,
        @Size(max = 20) String postalCode,
        @Size(max = 2) String countryCode
) {
}
