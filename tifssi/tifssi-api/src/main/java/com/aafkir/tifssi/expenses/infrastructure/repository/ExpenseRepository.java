package com.aafkir.tifssi.expenses.infrastructure.repository;

import com.aafkir.tifssi.expenses.domain.model.Expense;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByMissionIdOrderByExpenseDateAscIdAsc(Long missionId);

    List<Expense> findAllByMissionIdAndBillableOrderByExpenseDateAscIdAsc(Long missionId, boolean billable);

    List<Expense> findAllByProfileIdOrderByExpenseDateAscIdAsc(Long profileId);

    List<Expense> findAllByProfileIdAndBillableOrderByExpenseDateAscIdAsc(Long profileId, boolean billable);

    List<Expense> findAllByBillableOrderByExpenseDateAscIdAsc(boolean billable);

    List<Expense> findAllByMissionIdAndExpenseDateBetweenOrderByExpenseDateAscIdAsc(Long missionId, LocalDate startDate, LocalDate endDate);

    List<Expense> findAllByMissionIdInAndStatusOrderByExpenseDateAscIdAsc(List<Long> missionIds, ExpenseStatus status);

    boolean existsByMissionIdAndProfileIdAndExpenseDateAndComment(Long missionId, Long profileId, LocalDate expenseDate, String comment);
}
