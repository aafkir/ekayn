package com.aafkir.tifssi.timesheets.api.controller;
import com.aafkir.tifssi.timesheets.api.dto.request.TimesheetRejectRequest; import com.aafkir.tifssi.timesheets.api.dto.response.TimesheetResponse; import com.aafkir.tifssi.timesheets.application.service.TimesheetService; import jakarta.validation.Valid; import java.util.List; import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation; import io.swagger.v3.oas.annotations.tags.Tag;
@RestController @RequestMapping("/api/timesheets") @Tag(name="Timesheets", description="Feuilles CRA mensuelles et workflow de validation") public class TimesheetController {
 private final TimesheetService service; public TimesheetController(TimesheetService service){this.service=service;}
 @GetMapping public List<TimesheetResponse> findAll(@RequestParam(required=false) Long profileId,@RequestParam(required=false) Integer year,@RequestParam(required=false) Integer month){return service.findAll(profileId,year,month);}
 @GetMapping("/{id}") public TimesheetResponse get(@PathVariable Long id){return service.get(id);}
 @PostMapping("/{id}/submit") public TimesheetResponse submit(@PathVariable Long id){return service.submit(id);}
 @PostMapping("/{id}/validate") public TimesheetResponse validate(@PathVariable Long id,@RequestParam(required=false) Long managerId){return service.validate(id,managerId);}
 @PostMapping("/{id}/reject") public TimesheetResponse reject(@PathVariable Long id,@Valid @RequestBody TimesheetRejectRequest request){return service.reject(id,request);}
}
