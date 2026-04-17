package com.aafkir.tifssi.staffing.application.service;

import com.aafkir.tifssi.crm.application.service.CompanyService;
import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.domain.model.Contact;
import com.aafkir.tifssi.crm.infrastructure.repository.ContactRepository;
import com.aafkir.tifssi.projects.api.dto.response.ProjectResponse;
import com.aafkir.tifssi.projects.api.mapper.ProjectApiMapper;
import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.projects.infrastructure.repository.ProjectRepository;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.util.JsonNullableUtils;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.api.dto.request.NeedCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.request.NeedPatchRequest;
import com.aafkir.tifssi.staffing.api.dto.response.NeedResponse;
import com.aafkir.tifssi.staffing.api.mapper.NeedApiMapper;
import com.aafkir.tifssi.staffing.application.exception.InvalidNeedStateException;
import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
import com.aafkir.tifssi.staffing.domain.model.Need;
import com.aafkir.tifssi.staffing.infrastructure.repository.NeedRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NeedService {

    private final NeedRepository needRepository;
    private final CompanyService companyService;
    private final ContactRepository contactRepository;
    private final ProjectRepository projectRepository;
    private final NeedApiMapper needApiMapper;
    private final ProjectApiMapper projectApiMapper;
    private final EntityValidationService entityValidationService;

    public NeedService(
            NeedRepository needRepository,
            CompanyService companyService,
            ContactRepository contactRepository,
            ProjectRepository projectRepository,
            NeedApiMapper needApiMapper,
            ProjectApiMapper projectApiMapper,
            EntityValidationService entityValidationService
    ) {
        this.needRepository = needRepository;
        this.companyService = companyService;
        this.contactRepository = contactRepository;
        this.projectRepository = projectRepository;
        this.needApiMapper = needApiMapper;
        this.projectApiMapper = projectApiMapper;
        this.entityValidationService = entityValidationService;
    }

    @Transactional(readOnly = true)
    public List<NeedResponse> findAll() {
        return needRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(needApiMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NeedResponse findById(Long id) {
        return needApiMapper.toResponse(getNeed(id));
    }

    public NeedResponse create(NeedCreateRequest request) {
        Need need = needApiMapper.toEntity(request);
        need.setCompany(companyService.getCompany(request.companyId()));
        need.setContact(resolveContact(request.contactId()));
        validateNeedRelations(need);
        entityValidationService.validate(need);
        return needApiMapper.toResponse(needRepository.save(need));
    }

    public NeedResponse patch(Long id, NeedPatchRequest request) {
        Need need = getNeed(id);

        if (JsonNullableUtils.isDefined(request.getCompanyId())) {
            Long companyId = JsonNullableUtils.unwrap(request.getCompanyId());
            Company company = companyId == null ? null : companyService.getCompany(companyId);
            need.setCompany(company);
        }
        if (JsonNullableUtils.isDefined(request.getContactId())) {
            need.setContact(resolveContact(JsonNullableUtils.unwrap(request.getContactId())));
        }
        if (JsonNullableUtils.isDefined(request.getNeedReference())) {
            need.setNeedReference(JsonNullableUtils.unwrap(request.getNeedReference()));
        }
        if (JsonNullableUtils.isDefined(request.getTitle())) {
            need.setTitle(JsonNullableUtils.unwrap(request.getTitle()));
        }
        if (JsonNullableUtils.isDefined(request.getDescription())) {
            need.setDescription(JsonNullableUtils.unwrap(request.getDescription()));
        }
        if (JsonNullableUtils.isDefined(request.getStatus())) {
            need.setStatus(JsonNullableUtils.unwrap(request.getStatus()));
        }
        if (JsonNullableUtils.isDefined(request.getStartDate())) {
            need.setStartDate(JsonNullableUtils.unwrap(request.getStartDate()));
        }
        if (JsonNullableUtils.isDefined(request.getEndDate())) {
            need.setEndDate(JsonNullableUtils.unwrap(request.getEndDate()));
        }
        if (JsonNullableUtils.isDefined(request.getLocationName())) {
            need.setLocationName(JsonNullableUtils.unwrap(request.getLocationName()));
        }
        if (JsonNullableUtils.isDefined(request.getRemotePossible())) {
            Boolean remotePossible = JsonNullableUtils.unwrap(request.getRemotePossible());
            if (remotePossible == null) {
                throw new IllegalArgumentException("remotePossible cannot be null.");
            }
            need.setRemotePossible(remotePossible);
        }
        if (JsonNullableUtils.isDefined(request.getTargetDailyRate())) {
            need.setTargetDailyRate(JsonNullableUtils.unwrap(request.getTargetDailyRate()));
        }

        validateNeedRelations(need);
        entityValidationService.validate(need);
        return needApiMapper.toResponse(needRepository.save(need));
    }

    public void delete(Long id) {
        needRepository.delete(getNeed(id));
    }

    public ProjectResponse winAndCreateProject(Long needId) {
        Need need = getNeed(needId);
        validateNeedCanBeWon(need);

        need.setStatus(NeedStatus.WON);

        Project project = new Project();
        project.setCompany(need.getCompany());
        project.setContact(need.getContact());
        project.setOriginNeed(need);
        project.setProjectCode(buildProjectCode(need));
        project.setProjectName(need.getTitle());
        project.setDescription(need.getDescription());
        project.setStatus(ProjectStatus.DRAFT);
        project.setStartDate(need.getStartDate());
        project.setEndDate(need.getEndDate());

        entityValidationService.validate(need);
        entityValidationService.validate(project);

        needRepository.save(need);
        return projectApiMapper.toResponse(projectRepository.save(project));
    }

    public Need getNeed(Long id) {
        return needRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Need", id));
    }

    private Contact resolveContact(Long contactId) {
        if (contactId == null) {
            return null;
        }
        return contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", contactId));
    }

    private void validateNeedRelations(Need need) {
        if (need.getContact() != null
                && (need.getCompany() == null || !need.getContact().getCompany().getId().equals(need.getCompany().getId()))) {
            throw new IllegalArgumentException("Contact must belong to the same company as the need.");
        }
        if (need.getStartDate() != null && need.getEndDate() != null && need.getEndDate().isBefore(need.getStartDate())) {
            throw new IllegalArgumentException("endDate must be greater than or equal to startDate.");
        }
    }

    private void validateNeedCanBeWon(Need need) {
        if (!(need.getStatus() == NeedStatus.OPEN
                || need.getStatus() == NeedStatus.SHORTLIST
                || need.getStatus() == NeedStatus.PRESENTED)) {
            throw new InvalidNeedStateException(need.getId(), need.getStatus(), "Only OPEN, SHORTLIST or PRESENTED needs can be won.");
        }
        if (need.getProject() != null) {
            throw new InvalidNeedStateException(need.getId(), need.getStatus(), "A project already exists for this need.");
        }
    }

    private String buildProjectCode(Need need) {
        if (need.getNeedReference() != null && !need.getNeedReference().isBlank()) {
            return need.getNeedReference();
        }
        return "NEED-%d".formatted(need.getId());
    }
}
