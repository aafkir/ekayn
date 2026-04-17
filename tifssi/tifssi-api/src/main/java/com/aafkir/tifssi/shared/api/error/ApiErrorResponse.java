package com.aafkir.tifssi.shared.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(
        name = "ApiErrorResponse",
        description = "Format standardise des erreurs retournees par l'API.",
        example = """
                {
                  "timestamp": "2026-04-07T09:15:30Z",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "Validation failed.",
                  "path": "/api/companies",
                  "fieldErrors": [
                    {
                      "field": "legalName",
                      "message": "must not be blank"
                    }
                  ]
                }
                """
)
public record ApiErrorResponse(
        @Schema(description = "Date et heure UTC de l'erreur.", example = "2026-04-07T09:15:30Z")
        Instant timestamp,
        @Schema(description = "Code HTTP retourne.", example = "400")
        int status,
        @Schema(description = "Libelle HTTP retourne.", example = "Bad Request")
        String error,
        @Schema(description = "Message fonctionnel ou technique.", example = "Validation failed.")
        String message,
        @Schema(description = "Chemin HTTP ayant provoque l'erreur.", example = "/api/companies")
        String path,
        @Schema(description = "Liste des erreurs de validation par champ.")
        List<ApiFieldError> fieldErrors
) {
}
