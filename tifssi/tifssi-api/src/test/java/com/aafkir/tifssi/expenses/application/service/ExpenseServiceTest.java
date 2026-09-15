package com.aafkir.tifssi.expenses.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.expenses.api.dto.request.ExpenseCreateRequest;
import com.aafkir.tifssi.expenses.api.dto.request.ExpensePatchRequest;
import com.aafkir.tifssi.expenses.api.dto.response.ExpenseResponse;
import com.aafkir.tifssi.expenses.api.dto.response.ExpenseSummaryResponse;
import com.aafkir.tifssi.expenses.api.mapper.ExpenseApiMapper;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseCategory;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseStatus;
import com.aafkir.tifssi.expenses.domain.model.Expense;
import com.aafkir.tifssi.expenses.domain.model.ExpenseReport;
import com.aafkir.tifssi.expenses.infrastructure.repository.ExpenseRepository;
import com.aafkir.tifssi.projects.application.service.MissionService;
import com.aafkir.tifssi.projects.domain.enums.MissionStatus;
import com.aafkir.tifssi.projects.domain.enums.ProjectStatus;
import com.aafkir.tifssi.projects.domain.model.Mission;
import com.aafkir.tifssi.projects.domain.model.Project;
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
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private MissionService missionService;
    @Mock
    private ProfileService profileService;
    @Mock
    private ExpenseApiMapper expenseApiMapper;
    @Mock
    private EntityValidationService entityValidationService;
    @Mock
    private ExpenseReportService expenseReports;

    @InjectMocks
    private ExpenseService expenseService;

    private Mission mission;
    private Profile profile;

    @BeforeEach
    void stubMonthlyReportWorkflow() {
        ExpenseReport report = new ExpenseReport();
        report.setStatus(com.aafkir.tifssi.expenses.domain.enums.ExpenseReportStatus.DRAFT);
        org.mockito.Mockito.lenient().when(expenseReports.editable(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(report);
    }

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setId(10L);
        company.setLegalName("Acme Conseil");

        Project project = new Project();
        project.setId(20L);
        project.setCompany(company);
        project.setProjectCode("PRJ-EXP-001");
        project.setProjectName("Expenses MVP");
        project.setStatus(ProjectStatus.ACTIVE);

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
    void createShouldPersistExpenseWhenMissionAndProfileAreValid() {
        ExpenseCreateRequest request = new ExpenseCreateRequest(
                1L,
                2L,
                LocalDate.of(2026, 4, 10),
                ExpenseCategory.TRAVEL,
                new BigDecimal("125.50"),
                "eur",
                "  Taxi airport  ",
                "  https://cdn.example.com/receipt.pdf  ",
                ExpenseStatus.DRAFT,
                true
        );
        Expense mappedExpense = new Expense();
        mappedExpense.setExpenseDate(request.expenseDate());
        mappedExpense.setCategory(request.category());
        mappedExpense.setAmount(request.amount());
        mappedExpense.setStatus(request.status());
        mappedExpense.setBillable(request.billable());

        when(missionService.getMission(1L)).thenReturn(mission);
        when(profileService.getProfile(2L)).thenReturn(profile);
        when(expenseApiMapper.toEntity(request)).thenReturn(mappedExpense);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            expense.setId(99L);
            return expense;
        });
        when(expenseApiMapper.toResponse(any(Expense.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        ExpenseResponse response = expenseService.create(request);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.missionId()).isEqualTo(1L);
        assertThat(response.profileId()).isEqualTo(2L);
        assertThat(response.amount()).isEqualByComparingTo("125.50");
        assertThat(response.currency()).isEqualTo("EUR");
        assertThat(response.comment()).isEqualTo("Taxi airport");
        assertThat(response.receiptUrl()).isEqualTo("https://cdn.example.com/receipt.pdf");
        assertThat(response.billable()).isTrue();

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(expenseCaptor.capture());
        Expense savedExpense = expenseCaptor.getValue();
        assertThat(savedExpense.getMission()).isSameAs(mission);
        assertThat(savedExpense.getProfile()).isSameAs(profile);
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

        ExpenseCreateRequest request = new ExpenseCreateRequest(
                1L,
                3L,
                LocalDate.of(2026, 4, 10),
                ExpenseCategory.MEAL,
                new BigDecimal("25.00"),
                "EUR",
                null,
                null,
                ExpenseStatus.DRAFT,
                false
        );
        Expense mappedExpense = new Expense();
        mappedExpense.setExpenseDate(request.expenseDate());
        mappedExpense.setCategory(request.category());
        mappedExpense.setAmount(request.amount());
        mappedExpense.setStatus(request.status());
        mappedExpense.setBillable(request.billable());

        when(missionService.getMission(1L)).thenReturn(mission);
        when(profileService.getProfile(3L)).thenReturn(anotherProfile);
        when(expenseApiMapper.toEntity(request)).thenReturn(mappedExpense);

        assertThatThrownBy(() -> expenseService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("profileId must match the profile assigned to the mission.");

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void patchShouldUpdateMutableFields() {
        Expense expense = new Expense();
        expense.setId(5L);
        expense.setMission(mission);
        expense.setProfile(profile);
        expense.setExpenseDate(LocalDate.of(2026, 4, 10));
        expense.setCategory(ExpenseCategory.TRAVEL);
        expense.setAmount(new BigDecimal("125.50"));
        expense.setCurrency("EUR");
        expense.setComment("Initial");
        expense.setStatus(ExpenseStatus.DRAFT);
        expense.setBillable(true);

        ExpensePatchRequest request = new ExpensePatchRequest();
        request.setCategory(JsonNullable.of(ExpenseCategory.MEAL));
        request.setAmount(JsonNullable.of(new BigDecimal("45.00")));
        request.setCurrency(JsonNullable.of("usd"));
        request.setComment(JsonNullable.of("  Updated meal "));
        request.setReceiptUrl(JsonNullable.of(" https://cdn.example.com/meal.pdf "));
        request.setBillable(JsonNullable.of(false));
        request.setStatus(JsonNullable.of(ExpenseStatus.DRAFT));

        when(expenseRepository.findById(5L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(expenseApiMapper.toResponse(any(Expense.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        ExpenseResponse response = expenseService.patch(5L, request);

        assertThat(response.category()).isEqualTo(ExpenseCategory.MEAL);
        assertThat(response.amount()).isEqualByComparingTo("45.00");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.comment()).isEqualTo("Updated meal");
        assertThat(response.receiptUrl()).isEqualTo("https://cdn.example.com/meal.pdf");
        assertThat(response.billable()).isFalse();
        assertThat(response.status()).isEqualTo(ExpenseStatus.DRAFT);
    }

    @Test
    void getSummaryShouldAggregateByCurrencyAndBillableFlag() {
        Expense firstExpense = new Expense();
        firstExpense.setMission(mission);
        firstExpense.setProfile(profile);
        firstExpense.setCurrency("EUR");
        firstExpense.setAmount(new BigDecimal("100.00"));
        firstExpense.setBillable(true);

        Expense secondExpense = new Expense();
        secondExpense.setMission(mission);
        secondExpense.setProfile(profile);
        secondExpense.setCurrency("EUR");
        secondExpense.setAmount(new BigDecimal("20.00"));
        secondExpense.setBillable(false);

        Expense thirdExpense = new Expense();
        thirdExpense.setMission(mission);
        thirdExpense.setProfile(profile);
        thirdExpense.setCurrency("USD");
        thirdExpense.setAmount(new BigDecimal("50.00"));
        thirdExpense.setBillable(true);

        when(missionService.getMission(1L)).thenReturn(mission);
        when(expenseRepository.findAllByMissionIdAndExpenseDateBetweenOrderByExpenseDateAscIdAsc(
                1L,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        )).thenReturn(List.of(firstExpense, secondExpense, thirdExpense));

        ExpenseSummaryResponse response = expenseService.getSummary(1L, 4, 2026);

        assertThat(response.totalExpenses()).isEqualTo(3);
        assertThat(response.billableExpenses()).isEqualTo(2);
        assertThat(response.nonBillableExpenses()).isEqualTo(1);
        assertThat(response.totalsByCurrency()).hasSize(2);
        assertThat(response.totalsByCurrency().get(0).currency()).isEqualTo("EUR");
        assertThat(response.totalsByCurrency().get(0).totalAmount()).isEqualByComparingTo("120.00");
        assertThat(response.totalsByCurrency().get(0).billableAmount()).isEqualByComparingTo("100.00");
        assertThat(response.totalsByCurrency().get(0).nonBillableAmount()).isEqualByComparingTo("20.00");
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getMission() == null ? null : expense.getMission().getId(),
                expense.getProfile() == null ? null : expense.getProfile().getId(),
                expense.getExpenseDate(),
                expense.getCategory(),
                expense.getAmount(),
                expense.getCurrency(),
                expense.getComment(),
                expense.getReceiptUrl(),
                expense.getStatus(),
                expense.isBillable(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}
