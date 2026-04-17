package com.aafkir.tifssi.projects.api.controller;

import com.aafkir.tifssi.projects.api.dto.request.ProjectCreateRequest;
import com.aafkir.tifssi.projects.api.dto.request.ProjectPatchRequest;
import com.aafkir.tifssi.projects.api.dto.response.ProjectResponse;
import com.aafkir.tifssi.projects.application.service.ProjectService;
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
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Gestion des projets.")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @Operation(summary = "Lister les projets", description = "Retourne tous les projets disponibles.")
    @ApiResponse(
            responseCode = "200",
            description = "Liste des projets retournee avec succes.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjectResponse.class)))
    )
    public List<ProjectResponse> findAll() {
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lire un projet", description = "Retourne le detail d'un projet a partir de son identifiant.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Projet retourne avec succes.",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projet introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ProjectResponse findById(@PathVariable Long id) {
        return projectService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Creer un projet", description = "Cree un nouveau projet rattache a une entreprise.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Projet cree avec succes.",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entreprise, besoin d'origine ou contact introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Une contrainte d'unicite ou d'integrite empeche la creation.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse response = projectService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Modifier un projet", description = "Met a jour partiellement un projet existant.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Projet mis a jour avec succes.",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projet ou ressource de rattachement introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Une contrainte d'unicite ou d'integrite empeche la mise a jour.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ProjectResponse patch(@PathVariable Long id, @RequestBody ProjectPatchRequest request) {
        return projectService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un projet", description = "Supprime un projet a partir de son identifiant.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Projet supprime avec succes."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projet introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La suppression est refusee car le projet est encore reference.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
