package com.aafkir.tifssi.crm.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description = "Representation d'un contact client.",
        example = """
                {
                  "id": 1,
                  "companyId": 1,
                  "firstName": "Lea",
                  "lastName": "Martin",
                  "jobTitle": "Directrice achats",
                  "emailAddress": "lea.martin@acme.example",
                  "phoneNumber": "+33601020304",
                  "primaryContact": true,
                  "createdAt": "2026-04-07T09:15:30Z",
                  "updatedAt": "2026-04-07T09:15:30Z"
                }
                """
)
public record ContactResponse(
        Long id,
        Long companyId,
        String firstName,
        String lastName,
        String jobTitle,
        String emailAddress,
        String phoneNumber,
        boolean primaryContact,
        Instant createdAt,
        Instant updatedAt
) {
}
