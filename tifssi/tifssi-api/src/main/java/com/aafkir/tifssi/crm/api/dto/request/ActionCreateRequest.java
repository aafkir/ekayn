package com.aafkir.tifssi.crm.api.dto.request;

import com.aafkir.tifssi.crm.domain.enums.ActionStatus;
import com.aafkir.tifssi.crm.domain.enums.ActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(
        description = "Payload de creation d'une action CRM.",
        example = """
                {
                  "companyId": 12,
                  "contactId": 45,
                  "type": "CALL",
                  "subject": "Relance apres presentation",
                  "comment": "Appeler le DSI pour confirmer la date du prochain atelier.",
                  "dueDate": "2026-06-12",
                  "status": "TODO",
                  "responsibleName": "Nadia Mercier",
                  "nextStep": "Planifier un atelier de cadrage avec les achats"
                }
                """
)
public record ActionCreateRequest(
        @NotNull Long companyId,
        Long contactId,
        @NotNull ActionType type,
        @NotBlank @Size(max = 150) String subject,
        @Size(max = 4000) String comment,
        LocalDate dueDate,
        @NotNull ActionStatus status,
        @Size(max = 150) String responsibleName,
        @Size(max = 1000) String nextStep
) {
}
