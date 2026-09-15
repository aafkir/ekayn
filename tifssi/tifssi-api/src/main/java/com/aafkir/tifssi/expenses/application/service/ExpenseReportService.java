package com.aafkir.tifssi.expenses.application.service;

import com.aafkir.tifssi.expenses.api.dto.request.ExpenseReportRejectRequest;
import com.aafkir.tifssi.expenses.api.dto.response.ExpenseReportResponse;
import com.aafkir.tifssi.expenses.api.mapper.ExpenseReportApiMapper;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseReportStatus;
import com.aafkir.tifssi.expenses.domain.model.ExpenseReport;
import com.aafkir.tifssi.expenses.infrastructure.repository.ExpenseReportRepository;
import com.aafkir.tifssi.expenses.application.exception.ExpenseReportStateException;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import java.time.Instant; import java.time.LocalDate; import java.util.List;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class ExpenseReportService {
    private final ExpenseReportRepository repository; private final ExpenseReportApiMapper mapper; private final ProfileRepository profiles;
    public ExpenseReportService(ExpenseReportRepository repository, ExpenseReportApiMapper mapper, ProfileRepository profiles){this.repository=repository;this.mapper=mapper;this.profiles=profiles;}
    @Transactional(readOnly=true) public List<ExpenseReportResponse> findAll(Long profileId,Integer year,Integer month,ExpenseReportStatus status){
        if(year!=null&&year<2000) throw new IllegalArgumentException("year must be at least 2000."); if(month!=null&&(month<1||month>12)) throw new IllegalArgumentException("month must be between 1 and 12.");
        return repository.findAllByFilters(profileId,year,month,status).stream().map(mapper::toResponse).toList();
    }
    @Transactional(readOnly=true) public ExpenseReportResponse get(Long id){return mapper.toResponse(repository.findDetailedById(id).orElseThrow(()->new com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException("ExpenseReport",id)));}
    public ExpenseReport editable(Profile profile, LocalDate date){
        profiles.findForTimesheetCreation(profile.getId()).orElseThrow(()->new com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException("Profile",profile.getId()));
        ExpenseReport r=repository.findByProfileIdAndYearAndMonth(profile.getId(),date.getYear(),date.getMonthValue()).orElseGet(()->{ExpenseReport n=new ExpenseReport();n.setProfile(profile);n.setYear(date.getYear());n.setMonth(date.getMonthValue());return repository.save(n);});
        requireEditable(r); return r;
    }
    public void requireEditable(ExpenseReport r){if(r==null||!(r.getStatus()==ExpenseReportStatus.DRAFT||r.getStatus()==ExpenseReportStatus.REJECTED)) throw new ExpenseReportStateException("Only DRAFT or REJECTED expense reports can be edited.");}
    public ExpenseReportResponse submit(Long id){ExpenseReport r=getEntity(id); requireEditable(r); if(r.getExpenses().isEmpty()) throw new ExpenseReportStateException("An empty expense report cannot be submitted."); r.setStatus(ExpenseReportStatus.SUBMITTED);r.setSubmittedAt(Instant.now());r.setRejectedAt(null);r.setRejectedBy(null);r.setRejectionReason(null);return mapper.toResponse(r);}
    public ExpenseReportResponse validate(Long id,Long managerId){ExpenseReport r=submitted(id);r.setStatus(ExpenseReportStatus.VALIDATED);r.setValidatedAt(Instant.now());r.setValidatedBy(managerId);return mapper.toResponse(r);}
    public ExpenseReportResponse reject(Long id,ExpenseReportRejectRequest request){if(request==null||request.reason()==null||request.reason().isBlank()||request.reason().length()>2000) throw new IllegalArgumentException("A rejection reason of 1 to 2000 characters is required."); ExpenseReport r=submitted(id);r.setStatus(ExpenseReportStatus.REJECTED);r.setRejectedAt(Instant.now());r.setRejectedBy(request.managerId());r.setRejectionReason(request.reason().trim());return mapper.toResponse(r);}
    private ExpenseReport submitted(Long id){ExpenseReport r=getEntity(id);if(r.getStatus()!=ExpenseReportStatus.SUBMITTED) throw new ExpenseReportStateException("Only SUBMITTED expense reports can be validated or rejected.");return r;}
    private ExpenseReport getEntity(Long id){return repository.findById(id).orElseThrow(()->new com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException("ExpenseReport",id));}
}
