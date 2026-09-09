package com.aafkir.tifssi.crm.api.dto.response;

import com.aafkir.tifssi.crm.domain.enums.ActionStatus;
import com.aafkir.tifssi.crm.domain.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        description = "Representation d'une action CRM.",
        example = """
                {
                  "id": 101,
                  "companyId": 12,
                  "contactId": 45,
                  "type": "CALL",
                  "subject": "Relance apres presentation",
                  "comment": "Appeler le DSI pour confirmer la date du prochain atelier.",
                  "dueDate": "2026-06-12",
                  "status": "TODO",
                  "responsibleName": "Nadia Mercier",
                  "nextStep": "Planifier un atelier de cadrage avec les achats",
                  "createdAt": "2026-06-05T09:15:30Z",
                  "updatedAt": "2026-06-05T09:15:30Z"
                }
                """
)
public record ActionResponse(
        Long id,
        Long companyId,
        Long contactId,
        ActionType type,
        String subject,
        String comment,
        LocalDate dueDate,
        ActionStatus status,
        String responsibleName,
        String nextStep,
        Instant createdAt,
        Instant updatedAt
) {
}
