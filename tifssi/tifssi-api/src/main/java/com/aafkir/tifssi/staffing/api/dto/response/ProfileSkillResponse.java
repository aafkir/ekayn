package com.aafkir.tifssi.staffing.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Competence associee a un profil staffing.",
        example = """
                {
                  "skillId": 12,
                  "name": "PostgreSQL",
                  "level": 5,
                  "yearsOfExperience": 8,
                  "primarySkill": true
                }
                """
)
public record ProfileSkillResponse(
        @Schema(description = "Identifiant de la competence.", example = "12")
        Long skillId,
        @Schema(description = "Nom de la competence.", example = "PostgreSQL")
        String name,
        @Schema(description = "Niveau de maitrise renseigne pour ce profil.", example = "5")
        Integer level,
        @Schema(description = "Nombre d'annees d'experience sur cette competence.", example = "8")
        Integer yearsOfExperience,
        @Schema(description = "Indique la competence principale du profil.", example = "true")
        boolean primarySkill
) {
}
