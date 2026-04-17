package com.aafkir.tifssi.shared.api.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ApiFieldError",
        description = "Detail d'une erreur de validation sur un champ de la requete.",
        example = """
                {
                  "field": "legalName",
                  "message": "must not be blank"
                }
                """
)
public record ApiFieldError(
        @Schema(description = "Nom du champ invalide.", example = "legalName")
        String field,
        @Schema(description = "Message de validation associe.", example = "must not be blank")
        String message
) {
}
