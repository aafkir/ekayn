package com.aafkir.tifssi.staffing.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Contexte de l'utilisateur courant et de son profil métier")
public record CurrentUserProfileResponse(String userId, Long profileId, String firstName, String lastName, String role) {}
