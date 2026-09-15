package com.aafkir.tifssi.expenses.api.controller;
import com.aafkir.tifssi.expenses.api.dto.request.ExpenseReportRejectRequest;
import com.aafkir.tifssi.expenses.api.dto.request.ExpenseReportValidateRequest;
import com.aafkir.tifssi.expenses.api.dto.response.ExpenseReportResponse;
import com.aafkir.tifssi.expenses.application.service.ExpenseReportService;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseReportStatus;
import io.swagger.v3.oas.annotations.*; import io.swagger.v3.oas.annotations.tags.Tag; import jakarta.validation.Valid; import java.util.List; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/expense-reports") @Tag(name="Expense Reports",description="Notes de frais mensuelles et workflow indépendant des dépenses et absences")
public class ExpenseReportController {
 private final ExpenseReportService service; public ExpenseReportController(ExpenseReportService service){this.service=service;}
 @GetMapping @Operation(summary="Lister les notes de frais mensuelles") public List<ExpenseReportResponse> findAll(@RequestParam(required=false) Long profileId,@RequestParam(required=false) Integer year,@RequestParam(required=false) Integer month,@RequestParam(required=false) ExpenseReportStatus status){return service.findAll(profileId,year,month,status);}
 @GetMapping("/{id}") @Operation(summary="Consulter une note mensuelle et ses Expense") public ExpenseReportResponse get(@PathVariable Long id){return service.get(id);}
 @PostMapping("/{id}/submit") @Operation(summary="Soumettre toute la note mensuelle") public ExpenseReportResponse submit(@PathVariable Long id){return service.submit(id);}
 @PostMapping("/{id}/validate") @Operation(summary="Valider toute la note mensuelle") public ExpenseReportResponse validate(@PathVariable Long id,@RequestBody(required=false) ExpenseReportValidateRequest request,@RequestParam(required=false) Long managerId){return service.validate(id, request != null && request.managerId() != null ? request.managerId() : managerId);}
 @PostMapping("/{id}/reject") @Operation(summary="Rejeter toute la note avec motif") public ExpenseReportResponse reject(@PathVariable Long id,@Valid @RequestBody ExpenseReportRejectRequest request){return service.reject(id,request);}
}
