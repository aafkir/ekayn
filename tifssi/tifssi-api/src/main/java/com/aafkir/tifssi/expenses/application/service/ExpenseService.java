package com.aafkir.tifssi.expenses.application.service;

import com.aafkir.tifssi.expenses.api.dto.request.ExpenseCreateRequest;
import com.aafkir.tifssi.expenses.api.dto.request.ExpensePatchRequest;
import com.aafkir.tifssi.expenses.api.dto.response.ExpenseCurrencySummaryResponse;
import com.aafkir.tifssi.expenses.api.dto.response.ExpenseResponse;
import com.aafkir.tifssi.expenses.api.dto.response.ExpenseSummaryResponse;
import com.aafkir.tifssi.expenses.api.mapper.ExpenseApiMapper;
import com.aafkir.tifssi.expenses.domain.model.Expense;
import com.aafkir.tifssi.expenses.infrastructure.repository.ExpenseRepository;
import com.aafkir.tifssi.projects.application.service.MissionService;
import com.aafkir.tifssi.projects.domain.model.Mission;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.shared.application.util.JsonNullableUtils;
import com.aafkir.tifssi.shared.application.validation.EntityValidationService;
import com.aafkir.tifssi.staffing.application.service.ProfileService;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExpenseService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final ExpenseRepository expenseRepository;
    private final MissionService missionService;
    private final ProfileService profileService;
    private final ExpenseApiMapper expenseApiMapper;
    private final EntityValidationService entityValidationService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            MissionService missionService,
            ProfileService profileService,
            ExpenseApiMapper expenseApiMapper,
            EntityValidationService entityValidationService
    ) {
        this.expenseRepository = expenseRepository;
        this.missionService = missionService;
        this.profileService = profileService;
        this.expenseApiMapper = expenseApiMapper;
        this.entityValidationService = entityValidationService;
    }

    public ExpenseResponse create(ExpenseCreateRequest request) {
        Expense expense = expenseApiMapper.toEntity(request);
        expense.setMission(missionService.getMission(request.missionId()));
        expense.setProfile(profileService.getProfile(request.profileId()));
        expense.setCurrency(normalizeCurrency(request.currency()));
        expense.setComment(normalizeComment(request.comment()));
        expense.setReceiptUrl(normalizeReceiptUrl(request.receiptUrl()));

        validateExpenseRelations(expense);
        entityValidationService.validate(expense);
        return expenseApiMapper.toResponse(expenseRepository.save(expense));
    }

    public ExpenseResponse patch(Long id, ExpensePatchRequest request) {
        Expense expense = getExpense(id);

        if (JsonNullableUtils.isDefined(request.getMissionId())) {
            Long missionId = JsonNullableUtils.unwrap(request.getMissionId());
            Mission mission = missionId == null ? null : missionService.getMission(missionId);
            expense.setMission(mission);
        }
        if (JsonNullableUtils.isDefined(request.getProfileId())) {
            Long profileId = JsonNullableUtils.unwrap(request.getProfileId());
            Profile profile = profileId == null ? null : profileService.getProfile(profileId);
            expense.setProfile(profile);
        }
        if (JsonNullableUtils.isDefined(request.getExpenseDate())) {
            expense.setExpenseDate(JsonNullableUtils.unwrap(request.getExpenseDate()));
        }
        if (JsonNullableUtils.isDefined(request.getCategory())) {
            expense.setCategory(JsonNullableUtils.unwrap(request.getCategory()));
        }
        if (JsonNullableUtils.isDefined(request.getAmount())) {
            expense.setAmount(JsonNullableUtils.unwrap(request.getAmount()));
        }
        if (JsonNullableUtils.isDefined(request.getCurrency())) {
            expense.setCurrency(normalizeCurrency(JsonNullableUtils.unwrap(request.getCurrency())));
        }
        if (JsonNullableUtils.isDefined(request.getComment())) {
            expense.setComment(normalizeComment(JsonNullableUtils.unwrap(request.getComment())));
        }
        if (JsonNullableUtils.isDefined(request.getReceiptUrl())) {
            expense.setReceiptUrl(normalizeReceiptUrl(JsonNullableUtils.unwrap(request.getReceiptUrl())));
        }
        if (JsonNullableUtils.isDefined(request.getStatus())) {
            expense.setStatus(JsonNullableUtils.unwrap(request.getStatus()));
        }
        if (JsonNullableUtils.isDefined(request.getBillable())) {
            Boolean billable = JsonNullableUtils.unwrap(request.getBillable());
            if (billable == null) {
                throw new IllegalArgumentException("billable cannot be null.");
            }
            expense.setBillable(billable);
        }

        validateExpenseRelations(expense);
        entityValidationService.validate(expense);
        return expenseApiMapper.toResponse(expenseRepository.save(expense));
    }

    public void delete(Long id) {
        expenseRepository.delete(getExpense(id));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findAll(Long missionId, Long profileId, Boolean billable) {
        validateListFilters(missionId, profileId, billable);

        List<Expense> expenses;
        if (missionId != null) {
            missionService.getMission(missionId);
            expenses = billable == null
                    ? expenseRepository.findAllByMissionIdOrderByExpenseDateAscIdAsc(missionId)
                    : expenseRepository.findAllByMissionIdAndBillableOrderByExpenseDateAscIdAsc(missionId, billable);
        } else if (profileId != null) {
            profileService.getProfile(profileId);
            expenses = billable == null
                    ? expenseRepository.findAllByProfileIdOrderByExpenseDateAscIdAsc(profileId)
                    : expenseRepository.findAllByProfileIdAndBillableOrderByExpenseDateAscIdAsc(profileId, billable);
        } else {
            expenses = expenseRepository.findAllByBillableOrderByExpenseDateAscIdAsc(billable);
        }

        return expenses.stream()
                .map(expenseApiMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseSummaryResponse getSummary(Long missionId, Integer month, Integer year) {
        if (missionId == null) {
            throw new IllegalArgumentException("missionId is required.");
        }
        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12.");
        }
        if (year == null || year < 2000) {
            throw new IllegalArgumentException("year must be greater than or equal to 2000.");
        }

        missionService.getMission(missionId);

        YearMonth yearMonth = YearMonth.of(year, month);
        List<Expense> expenses = expenseRepository.findAllByMissionIdAndExpenseDateBetweenOrderByExpenseDateAscIdAsc(
                missionId,
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
        );

        Map<String, List<Expense>> expensesByCurrency = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCurrency));

        List<ExpenseCurrencySummaryResponse> totalsByCurrency = expensesByCurrency.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .map(entry -> new ExpenseCurrencySummaryResponse(
                        entry.getKey(),
                        sumAmounts(entry.getValue()),
                        sumBillableAmounts(entry.getValue(), true),
                        sumBillableAmounts(entry.getValue(), false)
                ))
                .toList();

        long billableExpenses = expenses.stream().filter(Expense::isBillable).count();
        long nonBillableExpenses = expenses.size() - billableExpenses;

        return new ExpenseSummaryResponse(
                missionId,
                month,
                year,
                expenses.size(),
                billableExpenses,
                nonBillableExpenses,
                totalsByCurrency
        );
    }

    public Expense getExpense(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
    }

    private void validateListFilters(Long missionId, Long profileId, Boolean billable) {
        if (missionId != null && profileId != null) {
            throw new IllegalArgumentException("missionId and profileId cannot be used together.");
        }
        if (missionId == null && profileId == null && billable == null) {
            throw new IllegalArgumentException("At least one filter must be provided.");
        }
    }

    private void validateExpenseRelations(Expense expense) {
        if (expense.getMission() == null) {
            throw new IllegalArgumentException("missionId cannot be null.");
        }
        if (expense.getProfile() == null) {
            throw new IllegalArgumentException("profileId cannot be null.");
        }
        if (!expense.getMission().getProfile().getId().equals(expense.getProfile().getId())) {
            throw new IllegalArgumentException("profileId must match the profile assigned to the mission.");
        }

        LocalDate expenseDate = expense.getExpenseDate();
        if (expenseDate == null) {
            return;
        }

        if (expense.getMission().getStartDate() != null && expenseDate.isBefore(expense.getMission().getStartDate())) {
            throw new IllegalArgumentException("expenseDate must be greater than or equal to mission startDate.");
        }
        if (expense.getMission().getEndDate() != null && expenseDate.isAfter(expense.getMission().getEndDate())) {
            throw new IllegalArgumentException("expenseDate must be less than or equal to mission endDate.");
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null) {
            return null;
        }

        String normalizedCurrency = currency.trim().toUpperCase(Locale.ROOT);
        return normalizedCurrency.isEmpty() ? null : normalizedCurrency;
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }

        String normalizedComment = comment.trim();
        return normalizedComment.isEmpty() ? null : normalizedComment;
    }

    private String normalizeReceiptUrl(String receiptUrl) {
        if (receiptUrl == null) {
            return null;
        }

        String normalizedReceiptUrl = receiptUrl.trim();
        return normalizedReceiptUrl.isEmpty() ? null : normalizedReceiptUrl;
    }

    private BigDecimal sumAmounts(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal sumBillableAmounts(List<Expense> expenses, boolean billable) {
        return expenses.stream()
                .filter(expense -> expense.isBillable() == billable)
                .map(Expense::getAmount)
                .reduce(ZERO, BigDecimal::add);
    }
}
