package com.aafkir.tifssi.timesheets.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Rejet d'un CRA mensuel complet ; aucune action sur les absences.")
public record TimesheetRejectRequest(
        @NotBlank @Size(max = 2000)
        @Schema(description = "Motif obligatoire du rejet.", example = "Merci de corriger les saisies du 12 septembre.") String reason,
        @Schema(description = "Identifiant du manager, facultatif pour la traçabilité.") Long managerId
) {}
