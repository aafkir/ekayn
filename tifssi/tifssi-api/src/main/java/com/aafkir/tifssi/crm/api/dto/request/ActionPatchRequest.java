package com.aafkir.tifssi.crm.api.dto.request;

import com.aafkir.tifssi.crm.domain.enums.ActionStatus;
import com.aafkir.tifssi.crm.domain.enums.ActionType;
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
        description = "Payload de mise a jour partielle d'une action CRM.",
        example = """
                {
                  "status": "DONE",
                  "comment": "Presentation effectuee, feedback positif.",
                  "nextStep": "Envoyer la proposition commerciale",
                  "dueDate": "2026-06-18"
                }
                """
)
public class ActionPatchRequest {

    private JsonNullable<Long> companyId = JsonNullable.undefined();
    private JsonNullable<Long> contactId = JsonNullable.undefined();
    private JsonNullable<ActionType> type = JsonNullable.undefined();
    private JsonNullable<String> subject = JsonNullable.undefined();
    private JsonNullable<String> comment = JsonNullable.undefined();
    private JsonNullable<LocalDate> dueDate = JsonNullable.undefined();
    private JsonNullable<ActionStatus> status = JsonNullable.undefined();
    private JsonNullable<String> responsibleName = JsonNullable.undefined();
    private JsonNullable<String> nextStep = JsonNullable.undefined();
}
