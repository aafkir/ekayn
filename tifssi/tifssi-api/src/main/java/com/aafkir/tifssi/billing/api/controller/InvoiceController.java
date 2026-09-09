package com.aafkir.tifssi.billing.api.controller;

import com.aafkir.tifssi.billing.api.dto.request.InvoiceLineCreateRequest;
import com.aafkir.tifssi.billing.api.dto.response.InvoiceDetailResponse;
import com.aafkir.tifssi.billing.application.service.InvoiceService;
import com.aafkir.tifssi.shared.api.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Billing")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/{invoiceId}")
    @Operation(summary = "Lire une facture", description = "Retourne le detail complet d'une facture et de ses lignes.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Facture retournee avec succes.",
                    content = @Content(schema = @Schema(implementation = InvoiceDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Facture introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public InvoiceDetailResponse findById(@PathVariable Long invoiceId) {
        return invoiceService.findById(invoiceId);
    }

    @PostMapping("/{invoiceId}/lines")
    @Operation(summary = "Ajouter une ligne manuelle", description = "Ajoute une ligne manuelle a une facture existante puis recalcule les totaux.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ligne ajoutee et facture recalculee avec succes.",
                    content = @Content(schema = @Schema(implementation = InvoiceDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Facture introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La facture n'est pas modifiable dans son etat actuel.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public InvoiceDetailResponse addLine(
            @PathVariable Long invoiceId,
            @Valid @RequestBody InvoiceLineCreateRequest request
    ) {
        return invoiceService.addManualLine(invoiceId, request);
    }

    @PostMapping("/{invoiceId}/generate-from-times-and-expenses")
    @Operation(
            summary = "Generer depuis les temps et frais valides",
            description = "Ajoute a la facture les lignes calculees a partir des temps valides et frais valides du projet."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Facture generee et recalculee avec succes.",
                    content = @Content(schema = @Schema(implementation = InvoiceDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Facture introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La generation est refusee par une regle metier ou une incoherence de donnees.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public InvoiceDetailResponse generateFromTimesAndExpenses(@PathVariable Long invoiceId) {
        return invoiceService.generateFromTimesAndExpenses(invoiceId);
    }

    @DeleteMapping("/{invoiceId}")
    @Operation(summary = "Supprimer une facture", description = "Supprime une facture a partir de son identifiant.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Facture supprimee avec succes."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Facture introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long invoiceId) {
        invoiceService.delete(invoiceId);
        return ResponseEntity.noContent().build();
    }
}
