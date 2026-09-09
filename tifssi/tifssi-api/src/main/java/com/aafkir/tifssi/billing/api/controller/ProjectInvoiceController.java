package com.aafkir.tifssi.billing.api.controller;

import com.aafkir.tifssi.billing.api.dto.request.InvoiceCreateRequest;
import com.aafkir.tifssi.billing.api.dto.response.InvoiceDetailResponse;
import com.aafkir.tifssi.billing.api.dto.response.InvoiceSummaryResponse;
import com.aafkir.tifssi.billing.application.service.InvoiceService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/projects/{projectId}/invoices")
@Tag(name = "Billing")
public class ProjectInvoiceController {

    private final InvoiceService invoiceService;

    public ProjectInvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @Operation(summary = "Creer une facture projet", description = "Cree une nouvelle facture pour le projet cible.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Facture creee avec succes.",
                    content = @Content(schema = @Schema(implementation = InvoiceDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projet introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Une contrainte d'unicite ou d'integrite empeche la creation.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<InvoiceDetailResponse> create(
            @PathVariable Long projectId,
            @Valid @RequestBody InvoiceCreateRequest request
    ) {
        InvoiceDetailResponse response = invoiceService.create(projectId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/invoices/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Lister les factures d'un projet", description = "Retourne toutes les factures rattachees au projet cible.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des factures retournee avec succes.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = InvoiceSummaryResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projet introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public List<InvoiceSummaryResponse> findAllByProject(@PathVariable Long projectId) {
        return invoiceService.findAllByProject(projectId);
    }
}
