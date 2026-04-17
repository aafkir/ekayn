package com.aafkir.tifssi.staffing.api.controller;

import com.aafkir.tifssi.projects.api.dto.response.ProjectResponse;
import com.aafkir.tifssi.shared.api.error.ApiErrorResponse;
import com.aafkir.tifssi.staffing.api.dto.request.NeedCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.request.NeedPatchRequest;
import com.aafkir.tifssi.staffing.api.dto.response.NeedResponse;
import com.aafkir.tifssi.staffing.application.service.NeedService;
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
@RequestMapping("/api/needs")
@Tag(name = "Staffing", description = "Gestion des besoins de staffing.")
public class NeedController {

    private final NeedService needService;

    public NeedController(NeedService needService) {
        this.needService = needService;
    }

    @GetMapping
    @Operation(
            summary = "Lister les besoins",
            description = "Retourne tous les besoins de staffing disponibles."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Liste des besoins retournee avec succes.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = NeedResponse.class)))
    )
    public List<NeedResponse> findAll() {
        return needService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lire un besoin",
            description = "Retourne le detail d'un besoin a partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Besoin retourne avec succes.",
                    content = @Content(schema = @Schema(implementation = NeedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Besoin introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public NeedResponse findById(@PathVariable Long id) {
        return needService.findById(id);
    }

    @PostMapping
    @Operation(
            summary = "Creer un besoin",
            description = "Cree un nouveau besoin de staffing rattache a une entreprise et, si present, a un contact."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Besoin cree avec succes.",
                    content = @Content(schema = @Schema(implementation = NeedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entreprise ou contact introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Une contrainte d'unicite ou d'integrite empeche la creation.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<NeedResponse> create(@Valid @RequestBody NeedCreateRequest request) {
        NeedResponse response = needService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{needId}/win-and-create-project")
    @Operation(
            summary = "Gagner un besoin et creer le projet",
            description = "Passe un besoin en gagne et cree le projet associe a partir des donnees du besoin."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Projet cree avec succes depuis le besoin.",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Besoin introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Le besoin n'est pas dans un etat compatible avec cette operation.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ProjectResponse> winAndCreateProject(@PathVariable Long needId) {
        ProjectResponse response = needService.winAndCreateProject(needId);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/projects/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Modifier un besoin",
            description = "Met a jour partiellement un besoin de staffing existant."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Besoin mis a jour avec succes.",
                    content = @Content(schema = @Schema(implementation = NeedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Besoin, entreprise ou contact introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Une contrainte d'unicite ou d'integrite empeche la mise a jour.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public NeedResponse patch(@PathVariable Long id, @RequestBody NeedPatchRequest request) {
        return needService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer un besoin",
            description = "Supprime un besoin de staffing a partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Besoin supprime avec succes."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Besoin introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La suppression est refusee car le besoin est encore reference.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        needService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
