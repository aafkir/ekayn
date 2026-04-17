package com.aafkir.tifssi.crm.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Payload de creation d'un contact rattache a une entreprise.",
        example = """
                {
                  "companyId": 1,
                  "firstName": "Lea",
                  "lastName": "Martin",
                  "jobTitle": "Directrice achats",
                  "emailAddress": "lea.martin@acme.example",
                  "phoneNumber": "+33601020304",
                  "primaryContact": true
                }
                """
)
public record ContactCreateRequest(
        @NotNull Long companyId,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 150) String jobTitle,
        @Email @Size(max = 150) String emailAddress,
        @Size(max = 50) String phoneNumber,
        boolean primaryContact
) {
}
