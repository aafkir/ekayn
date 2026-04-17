package com.aafkir.tifssi.absences.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.absences.api.dto.request.AbsenceCreateRequest;
import com.aafkir.tifssi.absences.api.dto.request.AbsencePatchRequest;
import com.aafkir.tifssi.absences.api.dto.response.AbsenceResponse;
import com.aafkir.tifssi.absences.api.dto.response.AbsenceSummaryResponse;
import com.aafkir.tifssi.absences.api.mapper.AbsenceApiMapper;
import com.aafkir.tifssi.absences.domain.enums.AbsenceStatus;
import com.aafkir.tifssi.absences.domain.enums.AbsenceType;
import com.aafkir.tifssi.absences.domain.model.Absence;
import com.aafkir.tifssi.absences.infrastructure.repository.AbsenceRepository;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.application.service.ProfileService;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;

@ExtendWith(MockitoExtension.class)
class AbsenceServiceTest {

    @Mock
    private AbsenceRepository absenceRepository;
    @Mock
    private ProfileService profileService;
    @Mock
    private AbsenceApiMapper absenceApiMapper;
    @Mock
    private EntityValidationService entityValidationService;

    @InjectMocks
    private AbsenceService absenceService;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = new Profile();
        profile.setId(2L);
        profile.setType(ProfileType.INTERNAL);
        profile.setFirstName("Lea");
        profile.setLastName("Martin");
        profile.setEmailAddress("lea@example.com");
        profile.setActive(true);
    }

    @Test
    void createShouldPersistAbsenceWhenDatesAreValid() {
        AbsenceCreateRequest request = new AbsenceCreateRequest(
                2L,
                AbsenceType.PAID_LEAVE,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                new BigDecimal("3.00"),
                "  Conges ete  ",
                AbsenceStatus.SUBMITTED
        );
        Absence mappedAbsence = new Absence();
        mappedAbsence.setType(request.type());
        mappedAbsence.setStartDate(request.startDate());
        mappedAbsence.setEndDate(request.endDate());
        mappedAbsence.setQuantity(request.quantity());
        mappedAbsence.setStatus(request.status());

        when(profileService.getProfile(2L)).thenReturn(profile);
        when(absenceApiMapper.toEntity(request)).thenReturn(mappedAbsence);
        when(absenceRepository.save(any(Absence.class))).thenAnswer(invocation -> {
            Absence absence = invocation.getArgument(0);
            absence.setId(99L);
            return absence;
        });
        when(absenceApiMapper.toResponse(any(Absence.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        AbsenceResponse response = absenceService.create(request);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.profileId()).isEqualTo(2L);
        assertThat(response.type()).isEqualTo(AbsenceType.PAID_LEAVE);
        assertThat(response.quantity()).isEqualByComparingTo("3.00");
        assertThat(response.comment()).isEqualTo("Conges ete");
        assertThat(response.status()).isEqualTo(AbsenceStatus.SUBMITTED);

        ArgumentCaptor<Absence> absenceCaptor = ArgumentCaptor.forClass(Absence.class);
        verify(absenceRepository).save(absenceCaptor.capture());
        assertThat(absenceCaptor.getValue().getProfile()).isSameAs(profile);
    }

    @Test
    void createShouldRejectEndDateBeforeStartDate() {
        AbsenceCreateRequest request = new AbsenceCreateRequest(
                2L,
                AbsenceType.RTT,
                LocalDate.of(2026, 7, 3),
                LocalDate.of(2026, 7, 1),
                new BigDecimal("1.00"),
                null,
                AbsenceStatus.DRAFT
        );
        Absence mappedAbsence = new Absence();
        mappedAbsence.setType(request.type());
        mappedAbsence.setStartDate(request.startDate());
        mappedAbsence.setEndDate(request.endDate());
        mappedAbsence.setQuantity(request.quantity());
        mappedAbsence.setStatus(request.status());

        when(profileService.getProfile(2L)).thenReturn(profile);
        when(absenceApiMapper.toEntity(request)).thenReturn(mappedAbsence);

        assertThatThrownBy(() -> absenceService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("endDate must be greater than or equal to startDate.");

        verify(absenceRepository, never()).save(any(Absence.class));
    }

    @Test
    void createShouldRejectQuantityGreaterThanPeriodLength() {
        AbsenceCreateRequest request = new AbsenceCreateRequest(
                2L,
                AbsenceType.SICK_LEAVE,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 2),
                new BigDecimal("3.00"),
                null,
                AbsenceStatus.SUBMITTED
        );
        Absence mappedAbsence = new Absence();
        mappedAbsence.setType(request.type());
        mappedAbsence.setStartDate(request.startDate());
        mappedAbsence.setEndDate(request.endDate());
        mappedAbsence.setQuantity(request.quantity());
        mappedAbsence.setStatus(request.status());

        when(profileService.getProfile(2L)).thenReturn(profile);
        when(absenceApiMapper.toEntity(request)).thenReturn(mappedAbsence);

        assertThatThrownBy(() -> absenceService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity cannot exceed the number of days in the selected period.");
    }

    @Test
    void patchShouldUpdateMutableFields() {
        Absence absence = new Absence();
        absence.setId(5L);
        absence.setProfile(profile);
        absence.setType(AbsenceType.PAID_LEAVE);
        absence.setStartDate(LocalDate.of(2026, 7, 1));
        absence.setEndDate(LocalDate.of(2026, 7, 3));
        absence.setQuantity(new BigDecimal("3.00"));
        absence.setComment("Initial");
        absence.setStatus(AbsenceStatus.DRAFT);

        AbsencePatchRequest request = new AbsencePatchRequest();
        request.setType(JsonNullable.of(AbsenceType.RTT));
        request.setQuantity(JsonNullable.of(new BigDecimal("2.50")));
        request.setComment(JsonNullable.of("  Ajustement workflow "));
        request.setStatus(JsonNullable.of(AbsenceStatus.APPROVED));

        when(absenceRepository.findById(5L)).thenReturn(Optional.of(absence));
        when(absenceRepository.save(any(Absence.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(absenceApiMapper.toResponse(any(Absence.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        AbsenceResponse response = absenceService.patch(5L, request);

        assertThat(response.type()).isEqualTo(AbsenceType.RTT);
        assertThat(response.quantity()).isEqualByComparingTo("2.50");
        assertThat(response.comment()).isEqualTo("Ajustement workflow");
        assertThat(response.status()).isEqualTo(AbsenceStatus.APPROVED);
    }

    @Test
    void findAllShouldFilterAbsencesByOverlappingPeriod() {
        Absence julyAbsence = new Absence();
        julyAbsence.setId(1L);
        julyAbsence.setProfile(profile);
        julyAbsence.setType(AbsenceType.PAID_LEAVE);
        julyAbsence.setStartDate(LocalDate.of(2026, 7, 10));
        julyAbsence.setEndDate(LocalDate.of(2026, 7, 12));
        julyAbsence.setQuantity(new BigDecimal("3.00"));
        julyAbsence.setStatus(AbsenceStatus.SUBMITTED);

        Absence augustAbsence = new Absence();
        augustAbsence.setId(2L);
        augustAbsence.setProfile(profile);
        augustAbsence.setType(AbsenceType.RTT);
        augustAbsence.setStartDate(LocalDate.of(2026, 8, 1));
        augustAbsence.setEndDate(LocalDate.of(2026, 8, 1));
        augustAbsence.setQuantity(new BigDecimal("1.00"));
        augustAbsence.setStatus(AbsenceStatus.DRAFT);

        when(profileService.getProfile(2L)).thenReturn(profile);
        when(absenceRepository.findAllByProfileIdOrderByStartDateAscIdAsc(2L)).thenReturn(List.of(julyAbsence, augustAbsence));
        when(absenceApiMapper.toResponse(julyAbsence)).thenReturn(toResponse(julyAbsence));

        List<AbsenceResponse> responses = absenceService.findAll(
                2L,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(1L);
    }

    @Test
    void getSummaryShouldAggregateOnlyOverlappingDaysOfYear() {
        Absence spanningAbsence = new Absence();
        spanningAbsence.setProfile(profile);
        spanningAbsence.setType(AbsenceType.PAID_LEAVE);
        spanningAbsence.setStartDate(LocalDate.of(2025, 12, 30));
        spanningAbsence.setEndDate(LocalDate.of(2026, 1, 2));
        spanningAbsence.setQuantity(new BigDecimal("4.00"));
        spanningAbsence.setStatus(AbsenceStatus.APPROVED);

        Absence julyAbsence = new Absence();
        julyAbsence.setProfile(profile);
        julyAbsence.setType(AbsenceType.RTT);
        julyAbsence.setStartDate(LocalDate.of(2026, 7, 14));
        julyAbsence.setEndDate(LocalDate.of(2026, 7, 14));
        julyAbsence.setQuantity(new BigDecimal("1.00"));
        julyAbsence.setStatus(AbsenceStatus.APPROVED);

        when(profileService.getProfile(2L)).thenReturn(profile);
        when(absenceRepository.findAllByProfileIdOrderByStartDateAscIdAsc(2L)).thenReturn(List.of(spanningAbsence, julyAbsence));

        AbsenceSummaryResponse response = absenceService.getSummary(2L, 2026);

        assertThat(response.profileId()).isEqualTo(2L);
        assertThat(response.year()).isEqualTo(2026);
        assertThat(response.totalAbsences()).isEqualTo(2);
        assertThat(response.totalQuantity()).isEqualByComparingTo("3.00");
    }

    private AbsenceResponse toResponse(Absence absence) {
        return new AbsenceResponse(
                absence.getId(),
                absence.getProfile() == null ? null : absence.getProfile().getId(),
                absence.getType(),
                absence.getStartDate(),
                absence.getEndDate(),
                absence.getQuantity(),
                absence.getComment(),
                absence.getStatus(),
                absence.getCreatedAt(),
                absence.getUpdatedAt()
        );
    }
}
