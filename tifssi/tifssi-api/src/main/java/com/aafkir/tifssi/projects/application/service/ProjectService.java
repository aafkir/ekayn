package com.aafkir.tifssi.projects.application.service;

import com.aafkir.tifssi.crm.application.service.CompanyService;
import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.domain.model.Contact;
import com.aafkir.tifssi.crm.infrastructure.repository.ContactRepository;
import com.aafkir.tifssi.projects.api.dto.request.ProjectCreateRequest;
import com.aafkir.tifssi.projects.api.dto.request.ProjectPatchRequest;
import com.aafkir.tifssi.projects.api.dto.response.ProjectResponse;
import com.aafkir.tifssi.projects.api.mapper.ProjectApiMapper;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.projects.infrastructure.repository.ProjectRepository;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.util.JsonNullableUtils;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.application.service.NeedService;
import com.aafkir.tifssi.staffing.domain.model.Need;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyService companyService;
    private final NeedService needService;
    private final ContactRepository contactRepository;
    private final ProjectApiMapper projectApiMapper;
    private final EntityValidationService entityValidationService;

    public ProjectService(
            ProjectRepository projectRepository,
            CompanyService companyService,
            NeedService needService,
            ContactRepository contactRepository,
            ProjectApiMapper projectApiMapper,
            EntityValidationService entityValidationService
    ) {
        this.projectRepository = projectRepository;
        this.companyService = companyService;
        this.needService = needService;
        this.contactRepository = contactRepository;
        this.projectApiMapper = projectApiMapper;
        this.entityValidationService = entityValidationService;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll() {
        return projectRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(projectApiMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(Long id) {
        return projectApiMapper.toResponse(getProject(id));
    }

    public ProjectResponse create(ProjectCreateRequest request) {
        Project project = projectApiMapper.toEntity(request);
        project.setCompany(companyService.getCompany(request.companyId()));
        project.setOriginNeed(resolveNeed(request.originNeedId()));
        project.setContact(resolveContact(request.contactId()));
        validateProjectRelations(project, null);
        entityValidationService.validate(project);
        return projectApiMapper.toResponse(projectRepository.save(project));
    }

    public ProjectResponse patch(Long id, ProjectPatchRequest request) {
        Project project = getProject(id);

        if (JsonNullableUtils.isDefined(request.getCompanyId())) {
            Long companyId = JsonNullableUtils.unwrap(request.getCompanyId());
            Company company = companyId == null ? null : companyService.getCompany(companyId);
            project.setCompany(company);
        }
        if (JsonNullableUtils.isDefined(request.getOriginNeedId())) {
            project.setOriginNeed(resolveNeed(JsonNullableUtils.unwrap(request.getOriginNeedId())));
        }
        if (JsonNullableUtils.isDefined(request.getContactId())) {
            project.setContact(resolveContact(JsonNullableUtils.unwrap(request.getContactId())));
        }
        if (JsonNullableUtils.isDefined(request.getProjectCode())) {
            project.setProjectCode(JsonNullableUtils.unwrap(request.getProjectCode()));
        }
        if (JsonNullableUtils.isDefined(request.getProjectName())) {
            project.setProjectName(JsonNullableUtils.unwrap(request.getProjectName()));
        }
        if (JsonNullableUtils.isDefined(request.getDescription())) {
            project.setDescription(JsonNullableUtils.unwrap(request.getDescription()));
        }
        if (JsonNullableUtils.isDefined(request.getStatus())) {
            project.setStatus(JsonNullableUtils.unwrap(request.getStatus()));
        }
        if (JsonNullableUtils.isDefined(request.getStartDate())) {
            project.setStartDate(JsonNullableUtils.unwrap(request.getStartDate()));
        }
        if (JsonNullableUtils.isDefined(request.getEndDate())) {
            project.setEndDate(JsonNullableUtils.unwrap(request.getEndDate()));
        }
        if (JsonNullableUtils.isDefined(request.getBudgetAmount())) {
            project.setBudgetAmount(JsonNullableUtils.unwrap(request.getBudgetAmount()));
        }

        validateProjectRelations(project, project.getId());
        entityValidationService.validate(project);
        return projectApiMapper.toResponse(projectRepository.save(project));
    }

    public void delete(Long id) {
        projectRepository.delete(getProject(id));
    }

    public Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    private Need resolveNeed(Long needId) {
        if (needId == null) {
            return null;
        }
        return needService.getNeed(needId);
    }

    private Contact resolveContact(Long contactId) {
        if (contactId == null) {
            return null;
        }
        return contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", contactId));
    }

    private void validateProjectRelations(Project project, Long currentProjectId) {
        if (project.getOriginNeed() != null
                && (project.getCompany() == null || !project.getOriginNeed().getCompany().getId().equals(project.getCompany().getId()))) {
            throw new IllegalArgumentException("Origin need must belong to the same company as the project.");
        }
        if (project.getContact() != null
                && (project.getCompany() == null || !project.getContact().getCompany().getId().equals(project.getCompany().getId()))) {
            throw new IllegalArgumentException("Contact must belong to the same company as the project.");
        }
        if (project.getOriginNeed() != null
                && project.getContact() != null
                && project.getOriginNeed().getContact() != null
                && !project.getOriginNeed().getContact().getId().equals(project.getContact().getId())) {
            throw new IllegalArgumentException("Project contact must match the origin need contact.");
        }

        if (project.getOriginNeed() != null
                && project.getOriginNeed().getProject() != null
                && !project.getOriginNeed().getProject().getId().equals(currentProjectId)) {
            throw new IllegalArgumentException("Origin need is already linked to another project.");
        }
        if (project.getStartDate() != null
                && project.getEndDate() != null
                && project.getEndDate().isBefore(project.getStartDate())) {
            throw new IllegalArgumentException("endDate must be greater than or equal to startDate.");
        }
    }
}
