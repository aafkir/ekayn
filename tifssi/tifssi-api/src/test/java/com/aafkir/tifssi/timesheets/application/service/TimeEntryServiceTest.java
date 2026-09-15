package com.aafkir.tifssi.timesheets.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.projects.application.service.MissionService;
import com.aafkir.tifssi.projects.domain.enums.MissionStatus;
import com.aafkir.tifssi.projects.domain.model.Mission;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.staffing.application.service.ProfileService;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.timesheets.api.dto.request.TimeEntryCreateRequest;
import com.aafkir.tifssi.timesheets.api.dto.request.TimeEntryPatchRequest;
import com.aafkir.tifssi.timesheets.api.dto.response.TimeEntryResponse;
import com.aafkir.tifssi.timesheets.api.dto.response.TimeEntrySummaryResponse;
import com.aafkir.tifssi.timesheets.api.mapper.TimeEntryApiMapper;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryStatus;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryUnitType;
import com.aafkir.tifssi.timesheets.domain.model.TimeEntry;
import com.aafkir.tifssi.timesheets.infrastructure.repository.TimeEntryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;

@ExtendWith(MockitoExtension.class)
class TimeEntryServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private MissionService missionService;
    @Mock
    private ProfileService profileService;
    @Mock
    private TimeEntryApiMapper timeEntryApiMapper;
    @Mock
    private EntityValidationService entityValidationService;

    @Mock
    private TimesheetService timesheets;

    @InjectMocks
    private TimeEntryService timeEntryService;

    private Mission mission;
    private Profile profile;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setId(10L);
        company.setLegalName("Acme Conseil");

        Project project = new Project();
        project.setId(20L);
        project.setCompany(company);
        project.setProjectCode("PRJ-001");
        project.setProjectName("Acme Platform");

        profile = new Profile();
        profile.setId(2L);
        profile.setType(ProfileType.INTERNAL);
        profile.setFirstName("Lea");
        profile.setLastName("Martin");
        profile.setEmailAddress("lea@example.com");
        profile.setActive(true);

        mission = new Mission();
        mission.setId(1L);
        mission.setProject(project);
        mission.setProfile(profile);
        mission.setRoleName("Developer");
        mission.setStatus(MissionStatus.ACTIVE);
        mission.setStartDate(LocalDate.of(2026, 4, 1));
        mission.setEndDate(LocalDate.of(2026, 4, 30));
    }

    @Test
    void createShouldPersistTimeEntryWhenMissionAndProfileAreValid() {
        TimeEntryCreateRequest request = new TimeEntryCreateRequest(
                1L,
                2L,
                LocalDate.of(2026, 4, 10),
                new BigDecimal("1.00"),
                TimeEntryUnitType.DAY,
                "  Delivery sprint  ",
                TimeEntryStatus.DRAFT
        );
        TimeEntry mappedTimeEntry = new TimeEntry();
        mappedTimeEntry.setWorkDate(request.workDate());
        mappedTimeEntry.setQuantity(request.quantity());
        mappedTimeEntry.setUnitType(request.unitType());
        mappedTimeEntry.setStatus(request.status());

        when(missionService.getMission(1L)).thenReturn(mission);
        when(profileService.getProfile(2L)).thenReturn(profile);
        when(timeEntryApiMapper.toEntity(request)).thenReturn(mappedTimeEntry);
        when(timesheets.editableFor(any(), any())).thenReturn(new com.aafkir.tifssi.timesheets.domain.model.Timesheet());
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> {
            TimeEntry timeEntry = invocation.getArgument(0);
            timeEntry.setId(99L);
            return timeEntry;
        });
        when(timeEntryApiMapper.toResponse(any(TimeEntry.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        TimeEntryResponse response = timeEntryService.create(request);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.missionId()).isEqualTo(1L);
        assertThat(response.profileId()).isEqualTo(2L);
        assertThat(response.quantity()).isEqualByComparingTo("1.00");
        assertThat(response.unitType()).isEqualTo(TimeEntryUnitType.DAY);
        assertThat(response.comment()).isEqualTo("Delivery sprint");
        assertThat(response.status()).isEqualTo(TimeEntryStatus.DRAFT);

        ArgumentCaptor<TimeEntry> timeEntryCaptor = ArgumentCaptor.forClass(TimeEntry.class);
        verify(timeEntryRepository).save(timeEntryCaptor.capture());
        TimeEntry savedTimeEntry = timeEntryCaptor.getValue();
        assertThat(savedTimeEntry.getMission()).isSameAs(mission);
        assertThat(savedTimeEntry.getProfile()).isSameAs(profile);
    }

    @Test
    void createShouldRejectProfileDifferentFromMissionProfile() {
        Profile anotherProfile = new Profile();
        anotherProfile.setId(3L);
        anotherProfile.setType(ProfileType.EXTERNAL);
        anotherProfile.setFirstName("Sara");
        anotherProfile.setLastName("Dupont");
        anotherProfile.setEmailAddress("sara@example.com");
        anotherProfile.setActive(true);

        TimeEntryCreateRequest request = new TimeEntryCreateRequest(
                1L,
                3L,
                LocalDate.of(2026, 4, 10),
                new BigDecimal("4.00"),
                TimeEntryUnitType.HOUR,
                null,
                TimeEntryStatus.DRAFT
        );
        TimeEntry mappedTimeEntry = new TimeEntry();
        mappedTimeEntry.setWorkDate(request.workDate());
        mappedTimeEntry.setQuantity(request.quantity());
        mappedTimeEntry.setUnitType(request.unitType());
        mappedTimeEntry.setStatus(request.status());

        when(missionService.getMission(1L)).thenReturn(mission);
        when(profileService.getProfile(3L)).thenReturn(anotherProfile);
        when(timeEntryApiMapper.toEntity(request)).thenReturn(mappedTimeEntry);

        assertThatThrownBy(() -> timeEntryService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("profileId must match the profile assigned to the mission.");

        verify(timeEntryRepository, never()).save(any(TimeEntry.class));
    }

    @Test
    void patchShouldUpdateMutableFields() {
        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setId(5L);
        timeEntry.setMission(mission);
        timeEntry.setProfile(profile);
        timeEntry.setWorkDate(LocalDate.of(2026, 4, 10));
        timeEntry.setQuantity(new BigDecimal("1.00"));
        timeEntry.setUnitType(TimeEntryUnitType.DAY);
        timeEntry.setComment("Initial");
        timeEntry.setStatus(TimeEntryStatus.DRAFT);

        TimeEntryPatchRequest request = new TimeEntryPatchRequest();
        request.setQuantity(JsonNullable.of(new BigDecimal("0.50")));
        request.setUnitType(JsonNullable.of(TimeEntryUnitType.HALF_DAY));
        request.setComment(JsonNullable.of("  Updated note "));
        request.setStatus(JsonNullable.of(TimeEntryStatus.DRAFT));

        when(timeEntryRepository.findById(5L)).thenReturn(Optional.of(timeEntry));
        when(timesheets.editableFor(any(), any())).thenReturn(new com.aafkir.tifssi.timesheets.domain.model.Timesheet());
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(timeEntryApiMapper.toResponse(any(TimeEntry.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        TimeEntryResponse response = timeEntryService.patch(5L, request);

        assertThat(response.quantity()).isEqualByComparingTo("0.50");
        assertThat(response.unitType()).isEqualTo(TimeEntryUnitType.HALF_DAY);
        assertThat(response.comment()).isEqualTo("Updated note");
        assertThat(response.status()).isEqualTo(TimeEntryStatus.DRAFT);
    }

    @Test
    void getSummaryShouldAggregateQuantitiesPerUnitType() {
        TimeEntry dayEntry = new TimeEntry();
        dayEntry.setMission(mission);
        dayEntry.setProfile(profile);
        dayEntry.setQuantity(new BigDecimal("2.00"));
        dayEntry.setUnitType(TimeEntryUnitType.DAY);

        TimeEntry halfDayEntry = new TimeEntry();
        halfDayEntry.setMission(mission);
        halfDayEntry.setProfile(profile);
        halfDayEntry.setQuantity(new BigDecimal("1.50"));
        halfDayEntry.setUnitType(TimeEntryUnitType.HALF_DAY);

        TimeEntry hourEntry = new TimeEntry();
        hourEntry.setMission(mission);
        hourEntry.setProfile(profile);
        hourEntry.setQuantity(new BigDecimal("7.50"));
        hourEntry.setUnitType(TimeEntryUnitType.HOUR);

        when(missionService.getMission(1L)).thenReturn(mission);
        when(timeEntryRepository.findAllByMissionIdAndWorkDateBetweenOrderByWorkDateAscIdAsc(
                1L,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        )).thenReturn(List.of(dayEntry, halfDayEntry, hourEntry));

        TimeEntrySummaryResponse response = timeEntryService.getSummary(1L, 4, 2026);

        assertThat(response.missionId()).isEqualTo(1L);
        assertThat(response.totalEntries()).isEqualTo(3);
        assertThat(response.totalDayQuantity()).isEqualByComparingTo("2.00");
        assertThat(response.totalHalfDayQuantity()).isEqualByComparingTo("1.50");
        assertThat(response.totalHourQuantity()).isEqualByComparingTo("7.50");
    }

    @ParameterizedTest
    @CsvSource({",", "1,", ",2", "1,2"})
    void findAllShouldApplyOptionalFiltersAndMapEntries(Long missionId, Long profileId) {
        TimeEntry entry = new TimeEntry();
        entry.setId(10L);
        entry.setMission(mission);
        entry.setProfile(profile);
        if (missionId != null) {
            when(missionService.getMission(missionId)).thenReturn(mission);
        }
        if (profileId != null) {
            when(profileService.getProfile(profileId)).thenReturn(profile);
        }
        when(timeEntryRepository.findAllByFilters(missionId, profileId)).thenReturn(List.of(entry));
        when(timeEntryApiMapper.toResponse(entry)).thenReturn(toResponse(entry));

        assertThat(timeEntryService.findAll(missionId, profileId)).containsExactly(toResponse(entry));

        if (missionId == null) {
            verifyNoInteractions(missionService);
        } else {
            verify(missionService).getMission(missionId);
        }
        if (profileId == null) {
            verifyNoInteractions(profileService);
        } else {
            verify(profileService).getProfile(profileId);
        }
    }

    @Test
    void findAllShouldPreserveMissionLookupFailure() {
        when(missionService.getMission(1L)).thenThrow(new ResourceNotFoundException("Mission", 1L));
        assertThatThrownBy(() -> timeEntryService.findAll(1L, null))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(timeEntryRepository);
    }

    @Test
    void findAllShouldPreserveProfileLookupFailureWithBothFilters() {
        when(missionService.getMission(1L)).thenReturn(mission);
        when(profileService.getProfile(2L)).thenThrow(new ResourceNotFoundException("Profile", 2L));
        assertThatThrownBy(() -> timeEntryService.findAll(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(timeEntryRepository);
    }

    private TimeEntryResponse toResponse(TimeEntry timeEntry) {
        return new TimeEntryResponse(
                timeEntry.getId(),
                timeEntry.getMission() == null ? null : timeEntry.getMission().getId(),
                timeEntry.getProfile() == null ? null : timeEntry.getProfile().getId(),
                timeEntry.getWorkDate(),
                timeEntry.getQuantity(),
                timeEntry.getUnitType(),
                timeEntry.getComment(),
                timeEntry.getStatus(),
                timeEntry.getCreatedAt(),
                timeEntry.getUpdatedAt()
        );
    }
}
