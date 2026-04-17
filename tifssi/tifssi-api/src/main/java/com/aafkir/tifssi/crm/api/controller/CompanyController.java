package com.aafkir.tifssi.crm.api.controller;

import com.aafkir.tifssi.crm.api.dto.request.CompanyCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.CompanyPatchRequest;
import com.aafkir.tifssi.crm.api.dto.response.CompanyResponse;
import com.aafkir.tifssi.crm.application.service.CompanyService;
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
@RequestMapping("/api/companies")
@Tag(name = "CRM", description = "Gestion des entreprises clientes.")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    @Operation(
            summary = "Lister les entreprises",
            description = "Retourne toutes les entreprises clientes connues par le module CRM."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Liste des entreprises retournee avec succes.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CompanyResponse.class)))
    )
    public List<CompanyResponse> findAll() {
        return companyService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lire une entreprise",
            description = "Retourne le detail d'une entreprise a partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entreprise retournee avec succes.",
                    content = @Content(schema = @Schema(implementation = CompanyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entreprise introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public CompanyResponse findById(@PathVariable Long id) {
        return companyService.findById(id);
    }

    @PostMapping
    @Operation(
            summary = "Creer une entreprise",
            description = "Cree une nouvelle entreprise cliente et retourne la ressource persistee."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Entreprise creee avec succes.",
                    content = @Content(schema = @Schema(implementation = CompanyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Une contrainte d'unicite ou d'integrite empeche la creation.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyCreateRequest request) {
        CompanyResponse response = companyService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Modifier une entreprise",
            description = "Met a jour partiellement une entreprise cliente existante."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entreprise mise a jour avec succes.",
                    content = @Content(schema = @Schema(implementation = CompanyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entreprise introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Une contrainte d'unicite ou d'integrite empeche la mise a jour.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public CompanyResponse patch(@PathVariable Long id, @RequestBody CompanyPatchRequest request) {
        return companyService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer une entreprise",
            description = "Supprime une entreprise cliente a partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Entreprise supprimee avec succes."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entreprise introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La suppression est refusee car la ressource est encore referencee.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
