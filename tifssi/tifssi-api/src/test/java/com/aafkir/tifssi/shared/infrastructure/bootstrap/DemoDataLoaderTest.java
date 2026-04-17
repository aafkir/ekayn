package com.aafkir.tifssi.shared.infrastructure.bootstrap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.crm.api.dto.request.CompanyCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.ContactCreateRequest;
import com.aafkir.tifssi.crm.api.dto.response.CompanyResponse;
import com.aafkir.tifssi.crm.api.dto.response.ContactResponse;
import com.aafkir.tifssi.crm.application.service.CompanyService;
import com.aafkir.tifssi.crm.application.service.ContactService;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.aafkir.tifssi.crm.infrastructure.repository.ContactRepository;
import com.aafkir.tifssi.projects.api.dto.request.MissionCreateRequest;
import com.aafkir.tifssi.projects.api.dto.request.ProjectCreateRequest;
import com.aafkir.tifssi.projects.api.dto.response.ProjectResponse;
import com.aafkir.tifssi.projects.application.service.MissionService;
import com.aafkir.tifssi.projects.application.service.ProjectService;
import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
import com.aafkir.tifssi.projects.infrastructure.repository.MissionRepository;
import com.aafkir.tifssi.projects.infrastructure.repository.ProjectRepository;
import com.aafkir.tifssi.staffing.api.dto.request.NeedCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.request.ProfileCreateRequest;
import com.aafkir.tifssi.staffing.api.dto.response.NeedResponse;
import com.aafkir.tifssi.staffing.api.dto.response.ProfileResponse;
import com.aafkir.tifssi.staffing.application.service.NeedService;
import com.aafkir.tifssi.staffing.application.service.ProfileService;
import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.infrastructure.repository.NeedRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

@ExtendWith(MockitoExtension.class)
class DemoDataLoaderTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private ContactRepository contactRepository;
    @Mock
    private NeedRepository needRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private MissionRepository missionRepository;
    @Mock
    private CompanyService companyService;
    @Mock
    private ContactService contactService;
    @Mock
    private NeedService needService;
    @Mock
    private ProfileService profileService;
    @Mock
    private ProjectService projectService;
    @Mock
    private MissionService missionService;

    @InjectMocks
    private DemoDataLoader demoDataLoader;

    @Test
    void runShouldSkipWhenBusinessDataAlreadyExists() throws Exception {
        when(companyRepository.count()).thenReturn(1L);

        demoDataLoader.run(new DefaultApplicationArguments(new String[0]));

        verifyNoInteractions(companyService, contactService, needService, profileService, projectService, missionService);
    }

    @Test
    void runShouldLoadMinimalDatasetWhenRepositoriesAreEmpty() throws Exception {
        when(companyService.create(any(CompanyCreateRequest.class))).thenReturn(
                companyResponse(1L, "Acme Conseil"),
                companyResponse(2L, "Globex Industrie")
        );
        when(contactService.create(any(ContactCreateRequest.class))).thenReturn(
                contactResponse(10L, 1L),
                contactResponse(11L, 1L),
                contactResponse(12L, 2L)
        );
        when(needService.create(any(NeedCreateRequest.class))).thenReturn(
                needResponse(20L, 1L, 10L, NeedStatus.WON),
                needResponse(21L, 2L, 12L, NeedStatus.WON),
                needResponse(22L, 1L, 11L, NeedStatus.OPEN)
        );
        when(profileService.create(any(ProfileCreateRequest.class))).thenReturn(
                profileResponse(30L, ProfileType.INTERNAL),
                profileResponse(31L, ProfileType.INTERNAL),
                profileResponse(32L, ProfileType.EXTERNAL),
                profileResponse(33L, ProfileType.EXTERNAL),
                profileResponse(34L, ProfileType.INTERNAL)
        );
        when(projectService.create(any(ProjectCreateRequest.class))).thenReturn(
                projectResponse(40L, 1L, 20L, 10L, "PRJ-2026-001"),
                projectResponse(41L, 2L, 21L, 12L, "PRJ-2026-002")
        );

        demoDataLoader.run(new DefaultApplicationArguments(new String[0]));

        verify(companyService, times(2)).create(any(CompanyCreateRequest.class));
        verify(contactService, times(3)).create(any(ContactCreateRequest.class));
        verify(needService, times(3)).create(any(NeedCreateRequest.class));
        verify(profileService, times(5)).create(any(ProfileCreateRequest.class));
        verify(projectService, times(2)).create(any(ProjectCreateRequest.class));
        verify(missionService, times(3)).create(any(MissionCreateRequest.class));
    }

    private CompanyResponse companyResponse(Long id, String legalName) {
        return new CompanyResponse(id, legalName, legalName, null, null, null, null, null, null, null, null, null, null, null);
    }

    private ContactResponse contactResponse(Long id, Long companyId) {
        return new ContactResponse(id, companyId, "Lea", "Martin", null, null, null, true, null, null);
    }

    private NeedResponse needResponse(Long id, Long companyId, Long contactId, NeedStatus status) {
        return new NeedResponse(id, companyId, contactId, null, null, "Need", null, status, null, null, null, false, null, null, null);
    }

    private ProfileResponse profileResponse(Long id, ProfileType type) {
        return new ProfileResponse(id, type, "Nina", "Dupont", "nina@example.com", null, null, null, true, new BigDecimal("700.00"), LocalDate.of(2026, 4, 15), null, null);
    }

    private ProjectResponse projectResponse(Long id, Long companyId, Long originNeedId, Long contactId, String projectCode) {
        return new ProjectResponse(
                id,
                companyId,
                originNeedId,
                contactId,
                projectCode,
                "Project",
                null,
                ProjectStatus.ACTIVE,
                null,
                null,
                null,
                null,
                null
        );
    }
}
