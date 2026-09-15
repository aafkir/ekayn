package com.aafkir.tifssi.absences.api.controller;

import com.aafkir.tifssi.absences.api.dto.request.AbsenceCreateRequest;
import com.aafkir.tifssi.absences.api.dto.request.AbsencePatchRequest;
import com.aafkir.tifssi.absences.api.dto.response.AbsenceResponse;
import com.aafkir.tifssi.absences.domain.enums.AbsenceStatus;
import com.aafkir.tifssi.absences.domain.enums.AbsenceType;
import com.aafkir.tifssi.absences.api.dto.response.AbsenceSummaryResponse;
import com.aafkir.tifssi.absences.application.service.AbsenceService;
import com.aafkir.tifssi.shared.api.error.ApiErrorResponse;
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
import java.time.LocalDate;
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
@RequestMapping("/api/absences")
@Tag(name = "Absences")
public class AbsenceController {

    private final AbsenceService absenceService;

    public AbsenceController(AbsenceService absenceService) {
        this.absenceService = absenceService;
    }

    @PostMapping
    @Operation(summary = "Creer une absence", description = "Cree une nouvelle absence rattachee a un profil.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Absence creee avec succes.",
                    content = @Content(schema = @Schema(implementation = AbsenceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La creation est refusee par une regle metier.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<AbsenceResponse> create(@Valid @RequestBody AbsenceCreateRequest request) {
        AbsenceResponse response = absenceService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Modifier une absence", description = "Met a jour partiellement une absence existante.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Absence mise a jour avec succes.",
                    content = @Content(schema = @Schema(implementation = AbsenceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Absence ou profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La mise a jour est refusee par une regle metier.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public AbsenceResponse patch(@PathVariable Long id, @RequestBody AbsencePatchRequest request) {
        return absenceService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une absence", description = "Supprime une absence a partir de son identifiant.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Absence supprimee avec succes."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Absence introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        absenceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "Lister les absences",
            description = "Retourne les absences filtrees par profil et, si fournie, par periode de dates."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des absences retournee avec succes.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AbsenceResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public List<AbsenceResponse> findAll(
            @Parameter(description = "Identifiant du profil pour filtrer les absences.")
            @RequestParam(required = false) Long profileId,
            @RequestParam(required = false) AbsenceStatus status,
            @RequestParam(required = false) AbsenceType type,
            @Parameter(description = "Date de debut de la periode de filtre.")
            @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "Date de fin de la periode de filtre.")
            @RequestParam(required = false) LocalDate endDate
    ) {
        return absenceService.findAll(profileId, status, type, startDate, endDate);
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Calculer le total des absences",
            description = "Retourne une synthese annuelle des absences d'un profil."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Synthese des absences retournee avec succes.",
                    content = @Content(schema = @Schema(implementation = AbsenceSummaryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public AbsenceSummaryResponse getSummary(
            @Parameter(description = "Identifiant du profil concerne par la synthese.")
            @RequestParam(required = false) Long profileId,
            @Parameter(description = "Annee de la synthese.")
            @RequestParam(required = false) Integer year
    ) {
        return absenceService.getSummary(profileId, year);
    }
}
