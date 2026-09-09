package com.aafkir.tifssi.crm.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

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
        String name,
        String relationType,
        String status,
        String sector,
        String managerName,
        String manager,
        String agency,
        String currentAction,
        String nextAction,
        LocalDate actionDate,
        LocalDate nextActionDate,
        String primaryContactName,
        String mainContactName,
        String contactName,
        String primaryContactRole,
        String contactRole,
        Integer contactsCount,
        Integer needsCount,
        Integer projectsCount,
        List<String> tags,
        String registrationNumber,
        String siret,
        String vatNumber,
        String websiteUrl,
        String website,
        String emailAddress,
        String email,
        String phoneNumber,
        String phone,
        String billingAddress,
        String address,
        String cityName,
        String city,
        String postalCode,
        String countryCode,
        String country,
        Instant createdAt,
        Instant updatedAt
) {
}
