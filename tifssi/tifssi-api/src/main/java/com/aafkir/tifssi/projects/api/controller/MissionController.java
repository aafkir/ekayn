package com.aafkir.tifssi.projects.api.controller;

import com.aafkir.tifssi.projects.api.dto.request.MissionCreateRequest;
import com.aafkir.tifssi.projects.api.dto.request.MissionPatchRequest;
import com.aafkir.tifssi.projects.api.dto.response.MissionResponse;
import com.aafkir.tifssi.projects.application.service.MissionService;
import com.aafkir.tifssi.shared.api.error.ApiErrorResponse;
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
@RequestMapping("/api/missions")
@Tag(name = "Projects", description = "Gestion des missions projet.")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping
    @Operation(summary = "Lister les missions", description = "Retourne toutes les missions disponibles.")
    @ApiResponse(
            responseCode = "200",
            description = "Liste des missions retournee avec succes.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MissionResponse.class)))
    )
    public List<MissionResponse> findAll() {
        return missionService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lire une mission", description = "Retourne le detail d'une mission a partir de son identifiant.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mission retournee avec succes.",
                    content = @Content(schema = @Schema(implementation = MissionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mission introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public MissionResponse findById(@PathVariable Long id) {
        return missionService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Creer une mission", description = "Cree une nouvelle mission rattachee a un projet et un profil.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Mission creee avec succes.",
                    content = @Content(schema = @Schema(implementation = MissionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projet ou profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<MissionResponse> create(@Valid @RequestBody MissionCreateRequest request) {
        MissionResponse response = missionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Modifier une mission", description = "Met a jour partiellement une mission existante.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Mission mise a jour avec succes.",
                    content = @Content(schema = @Schema(implementation = MissionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mission, projet ou profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public MissionResponse patch(@PathVariable Long id, @RequestBody MissionPatchRequest request) {
        return missionService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une mission", description = "Supprime une mission a partir de son identifiant.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mission supprimee avec succes."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mission introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La suppression est refusee car la mission est encore referencee.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        missionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
