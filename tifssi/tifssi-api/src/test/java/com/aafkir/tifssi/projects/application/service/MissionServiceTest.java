package com.aafkir.tifssi.projects.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.projects.api.dto.request.MissionCreateRequest;
import com.aafkir.tifssi.projects.api.dto.request.MissionPatchRequest;
import com.aafkir.tifssi.projects.api.dto.response.MissionResponse;
import com.aafkir.tifssi.projects.api.mapper.MissionApiMapper;
import com.aafkir.tifssi.projects.domain.enums.MissionStatus;
import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
import com.aafkir.tifssi.projects.domain.model.Mission;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.projects.infrastructure.repository.MissionRepository;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.application.service.ProfileService;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.domain.model.Profile;
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
class MissionServiceTest {

    @Mock
    private MissionRepository missionRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private ProfileService profileService;
    @Mock
    private MissionApiMapper missionApiMapper;
    @Mock
    private EntityValidationService entityValidationService;

    @InjectMocks
    private MissionService missionService;

    private Project project;
    private Profile profile;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(10L);
        project.setProjectCode("PRJ-2026-001");
        project.setProjectName("Plateforme staffing");
        project.setStatus(ProjectStatus.ACTIVE);

        profile = new Profile();
        profile.setId(20L);
        profile.setType(ProfileType.INTERNAL);
        profile.setFirstName("Nina");
        profile.setLastName("Dupont");
        profile.setEmailAddress("nina.dupont@tifssi.example");
        profile.setActive(true);
    }

    @Test
    void createShouldResolveProjectAndProfileAndPersistMission() {
        MissionCreateRequest request = new MissionCreateRequest(
                10L,
                20L,
                "Lead Backend",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 10, 31),
                new BigDecimal("800.00"),
                80,
                MissionStatus.ACTIVE
        );
        Mission mappedMission = new Mission();
        mappedMission.setRoleName(request.roleName());
        mappedMission.setStartDate(request.startDate());
        mappedMission.setEndDate(request.endDate());
        mappedMission.setDailyRate(request.dailyRate());
        mappedMission.setAllocationPercent(request.allocationPercent());
        mappedMission.setStatus(request.status());

        when(missionApiMapper.toEntity(request)).thenReturn(mappedMission);
        when(projectService.getProject(10L)).thenReturn(project);
        when(profileService.getProfile(20L)).thenReturn(profile);
        when(missionRepository.save(any(Mission.class))).thenAnswer(invocation -> {
            Mission savedMission = invocation.getArgument(0);
            savedMission.setId(70L);
            return savedMission;
        });
        when(missionApiMapper.toResponse(any(Mission.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        MissionResponse response = missionService.create(request);

        assertThat(response.id()).isEqualTo(70L);
        assertThat(response.projectId()).isEqualTo(10L);
        assertThat(response.profileId()).isEqualTo(20L);
        assertThat(response.dailyRate()).isEqualByComparingTo("800.00");
        verify(entityValidationService).validate(mappedMission);
    }

    @Test
    void patchShouldUpdateMutableFields() {
        Mission mission = new Mission();
        mission.setId(5L);
        mission.setProject(project);
        mission.setProfile(profile);
        mission.setRoleName("Backend Developer");
        mission.setStartDate(LocalDate.of(2026, 5, 1));
        mission.setEndDate(LocalDate.of(2026, 8, 31));
        mission.setDailyRate(new BigDecimal("750.00"));
        mission.setAllocationPercent(100);
        mission.setStatus(MissionStatus.ACTIVE);

        MissionPatchRequest request = new MissionPatchRequest();
        request.setDailyRate(JsonNullable.of(new BigDecimal("900.00")));
        request.setAllocationPercent(JsonNullable.of(60));
        request.setStatus(JsonNullable.of(MissionStatus.ENDED));

        when(missionRepository.findById(5L)).thenReturn(Optional.of(mission));
        when(missionRepository.save(any(Mission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(missionApiMapper.toResponse(any(Mission.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        MissionResponse response = missionService.patch(5L, request);

        assertThat(response.dailyRate()).isEqualByComparingTo("900.00");
        assertThat(response.allocationPercent()).isEqualTo(60);
        assertThat(response.status()).isEqualTo(MissionStatus.ENDED);
        verify(entityValidationService).validate(mission);
    }

    @Test
    void patchShouldRejectEndDateBeforeStartDate() {
        Mission mission = new Mission();
        mission.setId(5L);
        mission.setProject(project);
        mission.setProfile(profile);
        mission.setRoleName("Backend Developer");
        mission.setStartDate(LocalDate.of(2026, 5, 1));
        mission.setEndDate(LocalDate.of(2026, 8, 31));
        mission.setStatus(MissionStatus.ACTIVE);

        MissionPatchRequest request = new MissionPatchRequest();
        request.setStartDate(JsonNullable.of(LocalDate.of(2026, 6, 1)));
        request.setEndDate(JsonNullable.of(LocalDate.of(2026, 5, 1)));

        when(missionRepository.findById(5L)).thenReturn(Optional.of(mission));

        assertThatThrownBy(() -> missionService.patch(5L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("endDate must be greater than or equal to startDate.");

        verify(missionRepository, never()).save(any(Mission.class));
    }

    private MissionResponse toResponse(Mission target) {
        return new MissionResponse(
                target.getId(),
                target.getProject() == null ? null : target.getProject().getId(),
                target.getProfile() == null ? null : target.getProfile().getId(),
                target.getRoleName(),
                target.getStartDate(),
                target.getEndDate(),
                target.getDailyRate(),
                target.getAllocationPercent(),
                target.getStatus(),
                target.getCreatedAt(),
                target.getUpdatedAt()
        );
    }
}
