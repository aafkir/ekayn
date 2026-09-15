package com.aafkir.tifssi.timesheets.api.controller;

import com.aafkir.tifssi.timesheets.api.dto.request.TimesheetRejectRequest;
import com.aafkir.tifssi.timesheets.api.dto.response.TimesheetResponse;
import com.aafkir.tifssi.timesheets.application.service.TimesheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timesheets")
@Tag(name = "Timesheets", description = "CRA mensuels uniques par profil, année et mois. Workflow indépendant des absences.")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Paramètres ou motif invalides."),
        @ApiResponse(responseCode = "404", description = "Profil ou Timesheet introuvable."),
        @ApiResponse(responseCode = "409", description = "Transition interdite, feuille verrouillée ou modification concurrente.")
})
public class TimesheetController {
    private final TimesheetService service;

    public TimesheetController(TimesheetService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lister les CRA mensuels", description = "Filtres facultatifs et combinables profileId, year (>= 2000), month (1 à 12). Une feuille DRAFT est créée automatiquement lors de la première saisie du mois.")
    public List<TimesheetResponse> findAll(@RequestParam(required = false) Long profileId,
                                         @RequestParam(required = false) Integer year,
                                         @RequestParam(required = false) Integer month) {
        return service.findAll(profileId, year, month);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une Timesheet et toutes ses saisies", description = "Statut mensuel officiel et TimeEntries de toutes les missions du profil sur ce mois, triés par date. Les absences sont gérées séparément.")
    public TimesheetResponse get(@PathVariable Long id) { return service.get(id); }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Soumettre tout le CRA mensuel", description = "DRAFT ou REJECTED vers SUBMITTED. Une feuille vide ne peut pas être soumise. Les saisies sont ensuite verrouillées jusqu'à un éventuel rejet. Aucun statut TimeEntry ou Absence n'est modifié.")
    public TimesheetResponse submit(@PathVariable Long id) { return service.submit(id); }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Valider toute la Timesheet", description = "SUBMITTED vers VALIDATED en une action manager. La feuille reste en lecture seule. Aucun TimeEntry ni aucune absence n'est validé individuellement.")
    public TimesheetResponse validate(@PathVariable Long id,
                                     @Parameter(description = "Identifiant du manager, facultatif pour la traçabilité.")
                                     @RequestParam(required = false) Long managerId) {
        return service.validate(id, managerId);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Rejeter toute la Timesheet avec un motif", description = "SUBMITTED vers REJECTED. Motif obligatoire, 2000 caractères maximum. Le consultant peut corriger ses saisies puis soumettre de nouveau toute la feuille. Les absences restent inchangées.")
    public TimesheetResponse reject(@PathVariable Long id, @Valid @RequestBody TimesheetRejectRequest request) {
        return service.reject(id, request);
    }
}
