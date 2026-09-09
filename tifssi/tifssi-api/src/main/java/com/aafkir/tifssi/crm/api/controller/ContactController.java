package com.aafkir.tifssi.crm.api.controller;

import com.aafkir.tifssi.crm.api.dto.request.ContactCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.ContactPatchRequest;
import com.aafkir.tifssi.crm.api.dto.response.ContactResponse;
import com.aafkir.tifssi.crm.application.service.ContactService;
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
@RequestMapping("/api/contacts")
@Tag(name = "Contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    @Operation(
            summary = "Lister les contacts",
            description = "Retourne tous les contacts clients disponibles dans le CRM."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Liste des contacts retournee avec succes.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContactResponse.class)))
    )
    public List<ContactResponse> findAll() {
        return contactService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lire un contact",
            description = "Retourne le detail d'un contact client a partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Contact retourne avec succes.",
                    content = @Content(schema = @Schema(implementation = ContactResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Contact introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ContactResponse findById(@PathVariable Long id) {
        return contactService.findById(id);
    }

    @PostMapping
    @Operation(
            summary = "Creer un contact",
            description = "Cree un nouveau contact rattache a une entreprise cliente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Contact cree avec succes.",
                    content = @Content(schema = @Schema(implementation = ContactResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entreprise de rattachement introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody ContactCreateRequest request) {
        ContactResponse response = contactService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Modifier un contact",
            description = "Met a jour partiellement un contact client existant."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Contact mis a jour avec succes.",
                    content = @Content(schema = @Schema(implementation = ContactResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Contact ou entreprise de rattachement introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ContactResponse patch(@PathVariable Long id, @RequestBody ContactPatchRequest request) {
        return contactService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer un contact",
            description = "Supprime un contact client a partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contact supprime avec succes."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Contact introuvable.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
