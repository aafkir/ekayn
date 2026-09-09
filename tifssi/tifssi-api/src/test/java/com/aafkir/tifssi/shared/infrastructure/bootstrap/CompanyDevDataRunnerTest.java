package com.aafkir.tifssi.shared.infrastructure.bootstrap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.crm.api.dto.request.CompanyCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.ContactCreateRequest;
import com.aafkir.tifssi.crm.api.dto.request.ActionCreateRequest;
import com.aafkir.tifssi.crm.application.service.ActionService;
import com.aafkir.tifssi.crm.api.dto.response.CompanyResponse;
import com.aafkir.tifssi.crm.api.dto.response.ContactResponse;
import com.aafkir.tifssi.crm.application.service.CompanyService;
import com.aafkir.tifssi.crm.application.service.ContactService;
import com.aafkir.tifssi.crm.infrastructure.repository.ActionRepository;
import com.aafkir.tifssi.crm.infrastructure.repository.CompanyRepository;
import com.aafkir.tifssi.crm.infrastructure.repository.ContactRepository;
import com.aafkir.tifssi.absences.infrastructure.repository.AbsenceRepository;
import com.aafkir.tifssi.billing.infrastructure.repository.InvoiceRepository;
import com.aafkir.tifssi.expenses.infrastructure.repository.ExpenseRepository;
import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.projects.domain.model.Mission;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.projects.api.dto.request.MissionCreateRequest;
import com.aafkir.tifssi.projects.api.dto.request.ProjectCreateRequest;
import com.aafkir.tifssi.projects.api.dto.response.MissionResponse;
import com.aafkir.tifssi.projects.api.dto.response.ProjectResponse;
import com.aafkir.tifssi.projects.application.service.MissionService;
import com.aafkir.tifssi.projects.application.service.ProjectService;
import com.aafkir.tifssi.projects.domain.enums.MissionStatus;
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
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.domain.model.Skill;
import com.aafkir.tifssi.staffing.infrastructure.repository.NeedRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileSkillRepository;
import com.aafkir.tifssi.staffing.infrastructure.repository.SkillRepository;
import com.aafkir.tifssi.timesheets.infrastructure.repository.TimeEntryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

@ExtendWith(MockitoExtension.class)
class CompanyDevDataRunnerTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private ContactRepository contactRepository;
    @Mock
    private ActionRepository actionRepository;
    @Mock
    private NeedRepository needRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private ProfileSkillRepository profileSkillRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private MissionRepository missionRepository;
    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private AbsenceRepository absenceRepository;
    @Mock
    private CompanyService companyService;
    @Mock
    private ContactService contactService;
    @Mock
    private ActionService actionService;
    @Mock
    private NeedService needService;
    @Mock
    private ProfileService profileService;
    @Mock
    private ProjectService projectService;
    @Mock
    private MissionService missionService;

    @InjectMocks
    private CompanyDevDataRunner companyDevDataRunner;

    @Test
    void runShouldLoadMissingOperationalDatasetWhenDevCompanyDataAlreadyExists() throws Exception {
        when(companyRepository.existsByRegistrationNumberIn(any())).thenReturn(true);
        when(missionRepository.findByProjectProjectCodeAndProfileEmailAddressAndRoleName(any(), any(), any())).thenReturn(
                Optional.of(mission(50L, project(40L, "PRJ-2026-101"), profile(30L, "nina.dupont@tifssi-dev.example"), "Lead Backend")),
                Optional.of(mission(51L, project(40L, "PRJ-2026-101"), profile(31L, "hugo.bernard@tifssi-dev.example"), "Project Manager")),
                Optional.of(mission(52L, project(41L, "PRJ-2026-102"), profile(32L, "sara.benhamou@tifssi-dev.example"), "Architecte SI")),
                Optional.of(mission(53L, project(42L, "PRJ-2026-201"), profile(34L, "claire.morel@tifssi-dev.example"), "Business Analyst")),
                Optional.of(mission(54L, project(43L, "PRJ-2026-301"), profile(33L, "omar.el.idrissi@tifssi-dev.example"), "QA Lead DevOps")),
                Optional.of(mission(55L, project(44L, "PRJ-2026-401"), profile(35L, "mehdi.kaci@tifssi-dev.example"), "Data Engineer"))
        );
        when(projectRepository.findByProjectCode(any())).thenReturn(
                Optional.of(project(40L, "PRJ-2026-101")),
                Optional.of(project(41L, "PRJ-2026-102")),
                Optional.of(project(42L, "PRJ-2026-201")),
                Optional.of(project(43L, "PRJ-2026-301")),
                Optional.of(project(44L, "PRJ-2026-401")),
                Optional.of(project(45L, "PRJ-2026-501"))
        );
        when(profileRepository.findByEmailAddress(any())).thenReturn(
                Optional.of(profile(30L, "nina.dupont@tifssi-dev.example")),
                Optional.of(profile(31L, "hugo.bernard@tifssi-dev.example")),
                Optional.of(profile(32L, "sara.benhamou@tifssi-dev.example")),
                Optional.of(profile(33L, "omar.el.idrissi@tifssi-dev.example")),
                Optional.of(profile(34L, "claire.morel@tifssi-dev.example")),
                Optional.of(profile(35L, "mehdi.kaci@tifssi-dev.example"))
        );

        companyDevDataRunner.run(new DefaultApplicationArguments(new String[0]));

        verifyNoInteractions(companyService, contactService, actionService, needService, profileService, projectService, missionService, skillRepository, profileSkillRepository);
        verify(timeEntryRepository, times(42)).save(any());
        verify(expenseRepository, times(20)).save(any());
        verify(invoiceRepository, times(10)).save(any());
        verify(absenceRepository, times(18)).save(any());
    }

    @Test
    void runShouldLoadCompanyDatasetWhenOnlyNonDevDataExists() throws Exception {
        when(companyRepository.existsByRegistrationNumberIn(any())).thenReturn(false);
        when(companyService.create(any(CompanyCreateRequest.class))).thenReturn(
                companyResponse(1L, "Novalys Banque SA"),
                companyResponse(2L, "Mediance Sante SAS"),
                companyResponse(3L, "Equinoxe Cloud Services"),
                companyResponse(4L, "Atelier Data Factory SARL"),
                companyResponse(5L, "Helios Retail Group")
        );
        when(contactService.create(any(ContactCreateRequest.class))).thenReturn(
                contactResponse(10L, 1L),
                contactResponse(11L, 1L),
                contactResponse(12L, 2L),
                contactResponse(13L, 3L),
                contactResponse(14L, 4L),
                contactResponse(15L, 4L),
                contactResponse(16L, 5L)
        );
        when(actionService.create(any(ActionCreateRequest.class))).thenReturn(null);
        when(needService.create(any(NeedCreateRequest.class))).thenReturn(
                needResponse(20L, 1L, 10L, NeedStatus.WON),
                needResponse(21L, 1L, 11L, NeedStatus.PRESENTED),
                needResponse(22L, 1L, 10L, NeedStatus.OPEN),
                needResponse(23L, 2L, 12L, NeedStatus.SHORTLIST),
                needResponse(24L, 2L, 12L, NeedStatus.DRAFT),
                needResponse(25L, 3L, 13L, NeedStatus.WON),
                needResponse(26L, 3L, 13L, NeedStatus.OPEN),
                needResponse(27L, 4L, 14L, NeedStatus.WON),
                needResponse(28L, 4L, 15L, NeedStatus.PRESENTED),
                needResponse(29L, 5L, 16L, NeedStatus.WON)
        );
        when(profileService.create(any(ProfileCreateRequest.class))).thenReturn(
                profileResponse(30L, ProfileType.INTERNAL),
                profileResponse(31L, ProfileType.INTERNAL),
                profileResponse(32L, ProfileType.EXTERNAL),
                profileResponse(33L, ProfileType.EXTERNAL),
                profileResponse(34L, ProfileType.INTERNAL),
                profileResponse(35L, ProfileType.EXTERNAL),
                profileResponse(36L, ProfileType.EXTERNAL),
                profileResponse(37L, ProfileType.EXTERNAL),
                profileResponse(38L, ProfileType.EXTERNAL),
                profileResponse(39L, ProfileType.EXTERNAL)
        );
        when(profileRepository.getReferenceById(any())).thenReturn(new Profile());
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(projectService.create(any(ProjectCreateRequest.class))).thenReturn(
                projectResponse(40L, 1L, 20L, 10L, "PRJ-2026-101"),
                projectResponse(41L, 1L, null, 11L, "PRJ-2026-102"),
                projectResponse(42L, 2L, null, 12L, "PRJ-2026-201"),
                projectResponse(43L, 3L, 25L, 13L, "PRJ-2026-301"),
                projectResponse(44L, 4L, 27L, 14L, "PRJ-2026-401"),
                projectResponse(45L, 5L, 29L, 16L, "PRJ-2026-501")
        );
        when(missionService.create(any(MissionCreateRequest.class))).thenReturn(
                missionResponse(50L, 40L, 30L, "Lead Backend"),
                missionResponse(51L, 40L, 31L, "Project Manager"),
                missionResponse(52L, 41L, 32L, "Architecte SI"),
                missionResponse(53L, 42L, 34L, "Business Analyst"),
                missionResponse(54L, 43L, 33L, "QA Lead DevOps"),
                missionResponse(55L, 44L, 35L, "Data Engineer"),
                missionResponse(56L, 45L, 34L, "Support applicatif")
        );

        companyDevDataRunner.run(new DefaultApplicationArguments(new String[0]));

        verify(companyService, times(5)).create(any(CompanyCreateRequest.class));
        verify(contactService, times(7)).create(any(ContactCreateRequest.class));
        verify(actionService, times(15)).create(any(ActionCreateRequest.class));
        verify(needService, times(10)).create(any(NeedCreateRequest.class));
        verify(profileService, times(10)).create(any(ProfileCreateRequest.class));
        verify(skillRepository, times(23)).save(any(Skill.class));
        verify(profileSkillRepository, times(30)).save(any());
        verify(projectService, times(6)).create(any(ProjectCreateRequest.class));
        verify(missionService, times(7)).create(any(MissionCreateRequest.class));
        verify(timeEntryRepository, times(42)).save(any());
        verify(expenseRepository, times(20)).save(any());
        verify(invoiceRepository, times(10)).save(any());
        verify(absenceRepository, times(18)).save(any());
    }

    private CompanyResponse companyResponse(Long id, String legalName) {
        return new CompanyResponse(
                id,
                legalName,
                legalName,
                legalName,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                0,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
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

    private MissionResponse missionResponse(Long id, Long projectId, Long profileId, String roleName) {
        return new MissionResponse(
                id,
                projectId,
                profileId,
                roleName,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("700.00"),
                100,
                MissionStatus.ACTIVE,
                null,
                null
        );
    }

    private Profile profile(Long id, String emailAddress) {
        Profile profile = new Profile();
        profile.setId(id);
        profile.setType(ProfileType.INTERNAL);
        profile.setFirstName(emailAddress.substring(0, emailAddress.indexOf('.')));
        profile.setLastName("Dev");
        profile.setEmailAddress(emailAddress);
        profile.setActive(true);
        profile.setDefaultDailyRate(new BigDecimal("700.00"));
        profile.setAvailabilityDate(LocalDate.of(2026, 9, 1));
        return profile;
    }

    private Project project(Long id, String projectCode) {
        Company company = new Company();
        company.setId(1L);

        Project project = new Project();
        project.setId(id);
        project.setCompany(company);
        project.setProjectCode(projectCode);
        project.setProjectName("Project");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setBudgetAmount(new BigDecimal("100000.00"));
        project.setStartDate(LocalDate.of(2026, 1, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        return project;
    }

    private Mission mission(Long id, Project project, Profile profile, String roleName) {
        Mission mission = new Mission();
        mission.setId(id);
        mission.setProject(project);
        mission.setProfile(profile);
        mission.setRoleName(roleName);
        mission.setStartDate(LocalDate.of(2026, 8, 1));
        mission.setEndDate(LocalDate.of(2026, 12, 31));
        mission.setDailyRate(new BigDecimal("700.00"));
        mission.setAllocationPercent(100);
        mission.setStatus(MissionStatus.ACTIVE);
        return mission;
    }
}
