package com.aafkir.tifssi.crm.api.controller;

import com.aafkir.tifssi.crm.api.dto.request.ActionCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.ActionPatchRequest;
import com.aafkir.tifssi.crm.api.dto.response.ActionResponse;
import com.aafkir.tifssi.crm.application.service.ActionService;
import com.aafkir.tifssi.crm.domain.enums.ActionStatus;
import com.aafkir.tifssi.crm.domain.enums.ActionType;
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
@RequestMapping("/api/actions")
@Tag(name = "Actions")
public class ActionController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @GetMapping
    @Operation(
            summary = "Lister les actions",
            description = "Retourne les actions CRM, avec filtrage optionnel par societe, contact, statut ou type."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des actions retournee avec succes.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ActionResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Au moins un parametre de filtre est invalide.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Societe ou contact introuvable pour les filtres fournis.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur inattendue.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public List<ActionResponse> findAll(
            @Parameter(description = "Identifiant de la societe a laquelle les actions sont rattachees.", example = "12")
            @RequestParam(required = false) Long companyId,
            @Parameter(description = "Identifiant du contact rattache a l'action.", example = "45")
            @RequestParam(required = false) Long contactId,
            @Parameter(description = "Statut de l'action.", example = "TODO")
            @RequestParam(required = false) ActionStatus status,
            @Parameter(description = "Type de l'action.", example = "CALL")
            @RequestParam(required = false) ActionType type
    ) {
        return actionService.findAll(companyId, contactId, status, type);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lire une action",
            description = "Retourne le detail d'une action CRM a partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Action retournee avec succes.",
                    content = @Content(schema = @Schema(implementation = ActionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Action introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur inattendue.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ActionResponse findById(@PathVariable Long id) {
        return actionService.findById(id);
    }

    @PostMapping
    @Operation(
            summary = "Creer une action",
            description = "Cree une nouvelle action CRM rattachee a une societe et optionnellement a un contact."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Action creee avec succes.",
                    content = @Content(schema = @Schema(implementation = ActionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Le payload est invalide ou une regle metier simple n'est pas respectee.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Societe ou contact introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Une contrainte d'integrite empeche la creation.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur inattendue.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ActionResponse> create(@Valid @RequestBody ActionCreateRequest request) {
        ActionResponse response = actionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Modifier une action",
            description = "Met a jour partiellement une action CRM existante."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Action mise a jour avec succes.",
                    content = @Content(schema = @Schema(implementation = ActionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Le payload est invalide ou une regle metier simple n'est pas respectee.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Action, societe ou contact introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Une contrainte d'integrite empeche la mise a jour.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur inattendue.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ActionResponse patch(@PathVariable Long id, @RequestBody ActionPatchRequest request) {
        return actionService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer une action",
            description = "Supprime une action CRM a partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Action supprimee avec succes."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Action introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La suppression est refusee car l'action est encore referencee.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur inattendue.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        actionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
