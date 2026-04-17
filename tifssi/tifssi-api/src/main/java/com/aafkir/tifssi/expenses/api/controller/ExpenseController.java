package com.aafkir.tifssi.expenses.api.controller;

import com.aafkir.tifssi.expenses.api.dto.request.ExpenseCreateRequest;
import com.aafkir.tifssi.expenses.api.dto.request.ExpensePatchRequest;
import com.aafkir.tifssi.expenses.api.dto.response.ExpenseResponse;
import com.aafkir.tifssi.expenses.api.dto.response.ExpenseSummaryResponse;
import com.aafkir.tifssi.expenses.application.service.ExpenseService;
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
@RequestMapping("/api/expenses")
@Tag(name = "Expenses", description = "Gestion des notes de frais.")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @Operation(summary = "Creer une note de frais", description = "Cree une nouvelle note de frais rattachee a une mission et un profil.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Note de frais creee avec succes.",
                    content = @Content(schema = @Schema(implementation = ExpenseResponse.class))
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
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseCreateRequest request) {
        ExpenseResponse response = expenseService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Modifier une note de frais", description = "Met a jour partiellement une note de frais existante.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Note de frais mise a jour avec succes.",
                    content = @Content(schema = @Schema(implementation = ExpenseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note de frais, mission ou profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La mise a jour est refusee par une regle metier.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ExpenseResponse patch(@PathVariable Long id, @RequestBody ExpensePatchRequest request) {
        return expenseService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une note de frais", description = "Supprime une note de frais a partir de son identifiant.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Note de frais supprimee avec succes."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note de frais introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "Lister les notes de frais",
            description = "Retourne les notes de frais filtrees par mission, par profil et/ou par caractere refacturable."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des notes de frais retournee avec succes.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ExpenseResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mission ou profil introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public List<ExpenseResponse> findAll(
            @Parameter(description = "Identifiant de la mission pour filtrer les notes de frais.")
            @RequestParam(required = false) Long missionId,
            @Parameter(description = "Identifiant du profil pour filtrer les notes de frais.")
            @RequestParam(required = false) Long profileId,
            @Parameter(description = "Filtre sur le caractere refacturable des frais.")
            @RequestParam(required = false) Boolean billable
    ) {
        return expenseService.findAll(missionId, profileId, billable);
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Calculer les totaux des frais par periode",
            description = "Retourne une synthese des notes de frais d'une mission pour un mois et une annee donnes."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Synthese des frais retournee avec succes.",
                    content = @Content(schema = @Schema(implementation = ExpenseSummaryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Mission introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ExpenseSummaryResponse getSummary(
            @Parameter(description = "Identifiant de la mission concernee par la synthese.")
            @RequestParam(required = false) Long missionId,
            @Parameter(description = "Mois de la synthese, entre 1 et 12.")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Annee de la synthese.")
            @RequestParam(required = false) Integer year
    ) {
        return expenseService.getSummary(missionId, month, year);
    }
}
