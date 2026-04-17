package com.aafkir.tifssi.staffing.application.service;

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
import com.aafkir.tifssi.projects.api.dto.response.ProjectResponse;
import com.aafkir.tifssi.projects.api.mapper.ProjectApiMapper;
import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.projects.infrastructure.repository.ProjectRepository;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.api.mapper.NeedApiMapper;
import com.aafkir.tifssi.staffing.application.exception.InvalidNeedStateException;
import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
import com.aafkir.tifssi.staffing.domain.model.Need;
import com.aafkir.tifssi.staffing.infrastructure.repository.NeedRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NeedServiceTest {

    @Mock
    private NeedRepository needRepository;
    @Mock
    private CompanyService companyService;
    @Mock
    private ContactRepository contactRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private NeedApiMapper needApiMapper;
    @Mock
    private ProjectApiMapper projectApiMapper;
    @Mock
    private EntityValidationService entityValidationService;

    @InjectMocks
    private NeedService needService;

    private Need need;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setId(10L);

        Contact contact = new Contact();
        contact.setId(20L);
        contact.setCompany(company);

        need = new Need();
        need.setId(1L);
        need.setCompany(company);
        need.setContact(contact);
        need.setNeedReference("NEED-2026-001");
        need.setTitle("Consultant Java Senior");
        need.setDescription("Build backend Spring Boot.");
        need.setStatus(NeedStatus.OPEN);
        need.setStartDate(LocalDate.of(2026, 5, 1));
        need.setEndDate(LocalDate.of(2026, 12, 31));
        need.setCreatedAt(Instant.now());
        need.setUpdatedAt(Instant.now());
    }

    @Test
    void winAndCreateProjectShouldTransitionNeedAndCreateDraftProject() {
        when(needRepository.findById(1L)).thenReturn(Optional.of(need));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(99L);
            return project;
        });
        when(projectApiMapper.toResponse(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            return new ProjectResponse(
                    project.getId(),
                    project.getCompany().getId(),
                    project.getOriginNeed().getId(),
                    project.getContact().getId(),
                    project.getProjectCode(),
                    project.getProjectName(),
                    project.getDescription(),
                    project.getStatus(),
                    project.getStartDate(),
                    project.getEndDate(),
                    project.getBudgetAmount(),
                    project.getCreatedAt(),
                    project.getUpdatedAt()
            );
        });

        ProjectResponse response = needService.winAndCreateProject(1L);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.companyId()).isEqualTo(10L);
        assertThat(response.originNeedId()).isEqualTo(1L);
        assertThat(response.contactId()).isEqualTo(20L);
        assertThat(response.projectCode()).isEqualTo("NEED-2026-001");
        assertThat(response.projectName()).isEqualTo("Consultant Java Senior");
        assertThat(response.description()).isEqualTo("Build backend Spring Boot.");
        assertThat(response.status()).isEqualTo(ProjectStatus.DRAFT);
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(need.getStatus()).isEqualTo(NeedStatus.WON);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        Project savedProject = projectCaptor.getValue();
        assertThat(savedProject.getCompany()).isSameAs(need.getCompany());
        assertThat(savedProject.getContact()).isSameAs(need.getContact());
        assertThat(savedProject.getOriginNeed()).isSameAs(need);
    }

    @Test
    void winAndCreateProjectShouldRejectInvalidNeedStatus() {
        need.setStatus(NeedStatus.DRAFT);
        when(needRepository.findById(1L)).thenReturn(Optional.of(need));

        assertThatThrownBy(() -> needService.winAndCreateProject(1L))
                .isInstanceOf(InvalidNeedStateException.class)
                .hasMessageContaining("cannot be won");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void winAndCreateProjectShouldRejectNeedAlreadyLinkedToProject() {
        Project existingProject = new Project();
        existingProject.setId(50L);
        need.setProject(existingProject);
        when(needRepository.findById(1L)).thenReturn(Optional.of(need));

        assertThatThrownBy(() -> needService.winAndCreateProject(1L))
                .isInstanceOf(InvalidNeedStateException.class)
                .hasMessageContaining("already exists");

        verify(projectRepository, never()).save(any(Project.class));
    }
}
