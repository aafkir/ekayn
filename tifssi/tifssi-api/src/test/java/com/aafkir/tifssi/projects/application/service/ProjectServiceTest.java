package com.aafkir.tifssi.projects.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.crm.application.service.CompanyService;
import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.domain.model.Contact;
import com.aafkir.tifssi.crm.infrastructure.repository.ContactRepository;
import com.aafkir.tifssi.projects.api.dto.request.ProjectCreateRequest;
import com.aafkir.tifssi.projects.api.dto.request.ProjectPatchRequest;
import com.aafkir.tifssi.projects.api.dto.response.ProjectResponse;
import com.aafkir.tifssi.projects.api.mapper.ProjectApiMapper;
import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.projects.infrastructure.repository.ProjectRepository;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.application.service.NeedService;
import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
import com.aafkir.tifssi.staffing.domain.model.Need;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private CompanyService companyService;
    @Mock
    private NeedService needService;
    @Mock
    private ContactRepository contactRepository;
    @Mock
    private ProjectApiMapper projectApiMapper;
    @Mock
    private EntityValidationService entityValidationService;

    @InjectMocks
    private ProjectService projectService;

    private Company company;
    private Contact contact;
    private Need need;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(10L);
        company.setLegalName("Acme Conseil");

        contact = new Contact();
        contact.setId(20L);
        contact.setCompany(company);
        contact.setFirstName("Lea");
        contact.setLastName("Martin");
        contact.setPrimaryContact(true);

        need = new Need();
        need.setId(30L);
        need.setCompany(company);
        need.setContact(contact);
        need.setTitle("Plateforme staffing");
        need.setStatus(NeedStatus.WON);
    }

    @Test
    void createShouldResolveRelationsAndPersistProject() {
        ProjectCreateRequest request = new ProjectCreateRequest(
                10L,
                30L,
                20L,
                "PRJ-2026-001",
                "Plateforme staffing",
                "Projet gagne suite au besoin NEED-2026-001.",
                ProjectStatus.ACTIVE,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("120000.00")
        );
        Project mappedProject = new Project();
        mappedProject.setProjectCode(request.projectCode());
        mappedProject.setProjectName(request.projectName());
        mappedProject.setDescription(request.description());
        mappedProject.setStatus(request.status());
        mappedProject.setStartDate(request.startDate());
        mappedProject.setEndDate(request.endDate());
        mappedProject.setBudgetAmount(request.budgetAmount());

        when(projectApiMapper.toEntity(request)).thenReturn(mappedProject);
        when(companyService.getCompany(10L)).thenReturn(company);
        when(needService.getNeed(30L)).thenReturn(need);
        when(contactRepository.findById(20L)).thenReturn(Optional.of(contact));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project savedProject = invocation.getArgument(0);
            savedProject.setId(90L);
            return savedProject;
        });
        when(projectApiMapper.toResponse(any(Project.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        ProjectResponse response = projectService.create(request);

        assertThat(response.id()).isEqualTo(90L);
        assertThat(response.companyId()).isEqualTo(10L);
        assertThat(response.originNeedId()).isEqualTo(30L);
        assertThat(response.contactId()).isEqualTo(20L);
        verify(entityValidationService).validate(mappedProject);
    }

    @Test
    void createShouldRejectOriginNeedFromAnotherCompany() {
        Company anotherCompany = new Company();
        anotherCompany.setId(11L);
        anotherCompany.setLegalName("Globex Industrie");

        ProjectCreateRequest request = new ProjectCreateRequest(
                11L,
                30L,
                null,
                "PRJ-2026-002",
                "Migration ERP",
                null,
                ProjectStatus.DRAFT,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 10, 31),
                null
        );
        Project mappedProject = new Project();
        mappedProject.setProjectCode(request.projectCode());
        mappedProject.setProjectName(request.projectName());
        mappedProject.setStatus(request.status());
        mappedProject.setStartDate(request.startDate());
        mappedProject.setEndDate(request.endDate());

        when(projectApiMapper.toEntity(request)).thenReturn(mappedProject);
        when(companyService.getCompany(11L)).thenReturn(anotherCompany);
        when(needService.getNeed(30L)).thenReturn(need);

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Origin need must belong to the same company as the project.");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void patchShouldRejectContactThatDoesNotMatchOriginNeedContact() {
        Contact anotherContact = new Contact();
        anotherContact.setId(21L);
        anotherContact.setCompany(company);
        anotherContact.setFirstName("Karim");
        anotherContact.setLastName("Benali");

        Project project = new Project();
        project.setId(100L);
        project.setCompany(company);
        project.setOriginNeed(need);
        project.setContact(contact);
        project.setProjectCode("PRJ-2026-001");
        project.setProjectName("Plateforme staffing");
        project.setStatus(ProjectStatus.ACTIVE);

        ProjectPatchRequest request = new ProjectPatchRequest();
        request.setContactId(JsonNullable.of(21L));

        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(contactRepository.findById(21L)).thenReturn(Optional.of(anotherContact));

        assertThatThrownBy(() -> projectService.patch(100L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project contact must match the origin need contact.");

        verify(projectRepository, never()).save(any(Project.class));
    }

    private ProjectResponse toResponse(Project target) {
        return new ProjectResponse(
                target.getId(),
                target.getCompany() == null ? null : target.getCompany().getId(),
                target.getOriginNeed() == null ? null : target.getOriginNeed().getId(),
                target.getContact() == null ? null : target.getContact().getId(),
                target.getProjectCode(),
                target.getProjectName(),
                target.getDescription(),
                target.getStatus(),
                target.getStartDate(),
                target.getEndDate(),
                target.getBudgetAmount(),
                target.getCreatedAt(),
                target.getUpdatedAt()
        );
    }
}
