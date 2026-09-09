package com.aafkir.tifssi.timesheets.application.service;

import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.staffing.application.service.ProfileService;
import com.aafkir.tifssi.timesheets.api.dto.request.TimesheetRejectRequest;
import com.aafkir.tifssi.timesheets.api.dto.response.TimesheetResponse;
import com.aafkir.tifssi.timesheets.api.mapper.TimesheetApiMapper;
import com.aafkir.tifssi.timesheets.domain.enums.TimesheetStatus;
import com.aafkir.tifssi.timesheets.domain.model.Timesheet;
import com.aafkir.tifssi.timesheets.infrastructure.repository.TimesheetRepository;
import java.time.Instant; import java.util.List;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class TimesheetService {
 private final TimesheetRepository repository; private final TimesheetApiMapper mapper; private final ProfileService profileService;
 public TimesheetService(TimesheetRepository repository, TimesheetApiMapper mapper, ProfileService profileService){this.repository=repository;this.mapper=mapper;this.profileService=profileService;}
 @Transactional(readOnly=true) public List<TimesheetResponse> findAll(Long profileId,Integer year,Integer month){
  if(profileId!=null) profileService.getProfile(profileId);
  List<Timesheet> rows=(profileId!=null&&year!=null&&month!=null)?repository.findAllByProfileIdAndYearAndMonth(profileId,year,month):(profileId!=null?repository.findAllByProfileId(profileId):repository.findAll());
  if(profileId==null&&year!=null) rows=rows.stream().filter(t->year.equals(t.getYear())&&(month==null||month.equals(t.getMonth()))).toList();
  return rows.stream().map(mapper::toResponse).toList();
 }
 @Transactional(readOnly=true) public TimesheetResponse get(Long id){return mapper.toResponse(getEntity(id));}
 public TimesheetResponse submit(Long id){Timesheet t=getEntity(id); if(t.getStatus()!=TimesheetStatus.DRAFT&&t.getStatus()!=TimesheetStatus.REJECTED) throw new IllegalArgumentException("Only DRAFT or REJECTED timesheets can be submitted."); t.setStatus(TimesheetStatus.SUBMITTED); t.setSubmittedAt(Instant.now()); t.setRejectionReason(null); return mapper.toResponse(t);}
 public TimesheetResponse validate(Long id,Long managerId){Timesheet t=getEntity(id); if(t.getStatus()!=TimesheetStatus.SUBMITTED) throw new IllegalArgumentException("Only SUBMITTED timesheets can be validated."); t.setStatus(TimesheetStatus.VALIDATED); t.setValidatedAt(Instant.now()); t.setValidatedBy(managerId); return mapper.toResponse(t);}
 public TimesheetResponse reject(Long id,TimesheetRejectRequest request){Timesheet t=getEntity(id); if(t.getStatus()!=TimesheetStatus.SUBMITTED) throw new IllegalArgumentException("Only SUBMITTED timesheets can be rejected."); t.setStatus(TimesheetStatus.REJECTED); t.setRejectedAt(Instant.now()); t.setRejectedBy(request.managerId()); t.setRejectionReason(request.reason().trim()); return mapper.toResponse(t);}
 public Timesheet getEntity(Long id){return repository.findById(id).orElseThrow(()->new ResourceNotFoundException("Timesheet",id));}
}
