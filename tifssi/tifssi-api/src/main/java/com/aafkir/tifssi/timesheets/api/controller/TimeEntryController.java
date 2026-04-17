package com.aafkir.tifssi.timesheets.api.controller;

import com.aafkir.tifssi.shared.api.error.ApiErrorResponse;
import com.aafkir.tifssi.timesheets.api.dto.request.TimeEntryCreateRequest;
import com.aafkir.tifssi.timesheets.api.dto.request.TimeEntryPatchRequest;
import com.aafkir.tifssi.timesheets.api.dto.response.TimeEntryResponse;
import com.aafkir.tifssi.timesheets.api.dto.response.TimeEntrySummaryResponse;
import com.aafkir.tifssi.timesheets.application.service.TimeEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/time-entries")
@Tag(name = "Timesheets", description = "Gestion des saisies de temps.")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    public TimeEntryController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    @PostMapping
    @Operation(summary = "Creer une saisie de temps", description = "Cree une nouvelle saisie de temps rattachee a une mission et un profil.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Saisie de temps creee avec succes.",
                    content = @Content(schema = @Schema(implementation = TimeEntryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mission ou profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La creation est refusee par une regle metier.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<TimeEntryResponse> create(@Valid @RequestBody TimeEntryCreateRequest request) {
        TimeEntryResponse response = timeEntryService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Modifier une saisie de temps", description = "Met a jour partiellement une saisie de temps existante.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Saisie de temps mise a jour avec succes.",
                    content = @Content(schema = @Schema(implementation = TimeEntryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Saisie de temps, mission ou profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La mise a jour est refusee par une regle metier.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public TimeEntryResponse patch(@PathVariable Long id, @RequestBody TimeEntryPatchRequest request) {
        return timeEntryService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une saisie de temps", description = "Supprime une saisie de temps a partir de son identifiant.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Saisie de temps supprimee avec succes."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Saisie de temps introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "Lister les saisies de temps",
            description = "Retourne les saisies de temps filtrees par mission, par profil, ou l'ensemble si aucun filtre n'est fourni."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des saisies de temps retournee avec succes.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TimeEntryResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mission ou profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public List<TimeEntryResponse> findAll(
            @Parameter(description = "Identifiant de la mission pour filtrer les saisies de temps.")
            @RequestParam(required = false) Long missionId,
            @Parameter(description = "Identifiant du profil pour filtrer les saisies de temps.")
            @RequestParam(required = false) Long profileId
    ) {
        return timeEntryService.findAll(missionId, profileId);
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Calculer le total des temps par periode",
            description = "Retourne une synthese des saisies de temps pour une mission, un mois et une annee donnes."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Synthese des temps retournee avec succes.",
                    content = @Content(schema = @Schema(implementation = TimeEntrySummaryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mission introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public TimeEntrySummaryResponse getSummary(
            @Parameter(description = "Identifiant de la mission concernee par la synthese.")
            @RequestParam(required = false) Long missionId,
            @Parameter(description = "Mois de la synthese, entre 1 et 12.")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Annee de la synthese.")
            @RequestParam(required = false) Integer year
    ) {
        return timeEntryService.getSummary(missionId, month, year);
    }
}
