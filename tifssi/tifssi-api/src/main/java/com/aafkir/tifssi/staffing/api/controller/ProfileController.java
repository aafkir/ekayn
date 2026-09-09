package com.aafkir.tifssi.staffing.api.controller;

import com.aafkir.tifssi.shared.api.error.ApiErrorResponse;
import com.aafkir.tifssi.staffing.api.dto.request.ProfileCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.request.ProfilePatchRequest;
import com.aafkir.tifssi.staffing.api.dto.response.ProfileResponse;
import com.aafkir.tifssi.staffing.api.dto.response.ProfileSkillResponse;
import com.aafkir.tifssi.staffing.application.service.ProfileService;
import com.aafkir.tifssi.staffing.application.service.ProfileSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/profiles")
@Tag(name = "Profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileSkillService profileSkillService;

    public ProfileController(ProfileService profileService, ProfileSkillService profileSkillService) {
        this.profileService = profileService;
        this.profileSkillService = profileSkillService;
    }

    @GetMapping
    @Operation(
            summary = "Lister les profils",
            description = "Retourne tous les profils disponibles pour le staffing."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Liste des profils retournee avec succes.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProfileResponse.class)))
    )
    public List<ProfileResponse> findAll() {
        return profileService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lire un profil",
            description = "Retourne le detail d'un profil a partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profil retourne avec succes.",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ProfileResponse findById(@PathVariable Long id) {
        return profileService.findById(id);
    }

    @GetMapping("/{profileId}/skills")
    @Operation(
            summary = "Lister les competences d'un profil",
            description = "Retourne les competences associees a un profil, avec leur niveau, experience et indicateur de competence principale."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Competences du profil retournees avec succes.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProfileSkillResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public List<ProfileSkillResponse> findSkillsByProfileId(@PathVariable Long profileId) {
        return profileSkillService.findAllByProfileId(profileId);
    }

    @PostMapping
    @Operation(
            summary = "Creer un profil",
            description = "Cree un nouveau profil utilisable pour le staffing et les projets. "
                    + "Les champs persistés par le domaine sont type, firstName, lastName, email, phone, role, seniority, active, defaultDailyRate et availabilityDate."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload invalide. Les contraintes Bean Validation du DTO de creation ne sont pas respectees.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "Profil cree avec succes.",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Une contrainte d'unicite ou d'integrite empeche la creation.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ProfileResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Payload de creation d'un profil. Les champs de maquette non modelises dans le backend, comme domain, tags, notes ou une saisie libre de skills/keySkills, ne sont pas acceptes par cette operation."
            )
            @Valid
            @org.springframework.web.bind.annotation.RequestBody ProfileCreateRequest request
    ) {
        ProfileResponse response = profileService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{profileId}")
    @Operation(
            summary = "Modifier un profil",
            description = "Met a jour partiellement un profil existant. Les champs skills, domain, tags et notes ne sont pas pris en charge par cette operation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload invalide ou etat final du profil non valide.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Profil mis a jour avec succes.",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Une contrainte d'unicite ou d'integrite empeche la mise a jour.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ProfileResponse patch(
            @PathVariable Long profileId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Champs persistants du profil a modifier partiellement : type, firstName, lastName, email, phone, role, seniority, active, defaultDailyRate et availabilityDate."
            )
            @Valid
            @RequestBody ProfilePatchRequest request
    ) {
        return profileService.patch(profileId, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer un profil",
            description = "Supprime un profil a partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profil supprime avec succes."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La suppression est refusee car le profil est encore reference.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        profileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
