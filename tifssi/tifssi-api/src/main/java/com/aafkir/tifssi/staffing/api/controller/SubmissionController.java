package com.aafkir.tifssi.staffing.api.controller;

import com.aafkir.tifssi.shared.api.error.ApiErrorResponse;
import com.aafkir.tifssi.staffing.api.dto.request.SubmissionCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.request.SubmissionPatchRequest;
import com.aafkir.tifssi.staffing.api.dto.request.SubmissionStatusPatchRequest;
import com.aafkir.tifssi.staffing.api.dto.response.SubmissionResponse;
import com.aafkir.tifssi.staffing.application.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submissions")
@Tag(name = "Submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping
    @Operation(
            summary = "Lister les positionnements",
            description = "Retourne les positionnements filtres par besoin, par profil, ou l'ensemble si aucun filtre n'est fourni."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Liste des positionnements retournee avec succes.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SubmissionResponse.class)))
    )
    public List<SubmissionResponse> findAll(
            @Parameter(description = "Identifiant du besoin pour filtrer les positionnements.")
            @RequestParam(required = false) Long needId,
            @Parameter(description = "Identifiant du profil pour filtrer les positionnements.")
            @RequestParam(required = false) Long profileId
    ) {
        if (needId != null) {
            return submissionService.findAllByNeed(needId);
        }
        if (profileId != null) {
            return submissionService.findAllByProfile(profileId);
        }
        return submissionService.findAll(needId, profileId);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lire un positionnement",
            description = "Retourne le detail d'un positionnement a partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Positionnement retourne avec succes.",
                    content = @Content(schema = @Schema(implementation = SubmissionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Positionnement introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public SubmissionResponse findById(@PathVariable Long id) {
        return submissionService.findById(id);
    }

    @PostMapping
    @Operation(
            summary = "Creer un positionnement",
            description = "Cree un nouveau positionnement entre un besoin et un profil."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Positionnement cree avec succes.",
                    content = @Content(schema = @Schema(implementation = SubmissionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Besoin ou profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Le positionnement est en conflit avec une regle metier ou une contrainte d'unicite.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<SubmissionResponse> create(@Valid @RequestBody SubmissionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(submissionService.create(request));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Changer le statut d'un positionnement",
            description = "Met a jour uniquement le statut d'un positionnement existant."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Statut du positionnement mis a jour avec succes.",
                    content = @Content(schema = @Schema(implementation = SubmissionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Positionnement introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La transition de statut n'est pas autorisee.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public SubmissionResponse patchStatus(@PathVariable Long id, @Valid @RequestBody SubmissionStatusPatchRequest request) {
        return submissionService.patchStatus(id, request);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Modifier un positionnement",
            description = "Met a jour partiellement un positionnement existant, par exemple le TJM propose ou le commentaire."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Positionnement mis a jour avec succes.",
                    content = @Content(schema = @Schema(implementation = SubmissionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Positionnement introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La mise a jour viole une regle metier.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public SubmissionResponse patch(@PathVariable Long id, @RequestBody SubmissionPatchRequest request) {
        return submissionService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer un positionnement",
            description = "Supprime un positionnement a partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Positionnement supprime avec succes."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Positionnement introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        submissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
