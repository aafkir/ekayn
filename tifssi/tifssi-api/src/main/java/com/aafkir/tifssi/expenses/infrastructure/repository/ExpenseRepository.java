package com.aafkir.tifssi.expenses.infrastructure.repository;

import com.aafkir.tifssi.expenses.domain.model.Expense;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseStatus;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseReportStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByMissionIdOrderByExpenseDateAscIdAsc(Long missionId);

    List<Expense> findAllByMissionIdAndBillableOrderByExpenseDateAscIdAsc(Long missionId, boolean billable);

    List<Expense> findAllByProfileIdOrderByExpenseDateAscIdAsc(Long profileId);

    List<Expense> findAllByProfileIdAndBillableOrderByExpenseDateAscIdAsc(Long profileId, boolean billable);

    List<Expense> findAllByBillableOrderByExpenseDateAscIdAsc(boolean billable);

    List<Expense> findAllByMissionIdAndExpenseDateBetweenOrderByExpenseDateAscIdAsc(Long missionId, LocalDate startDate, LocalDate endDate);

    List<Expense> findAllByMissionIdInAndStatusOrderByExpenseDateAscIdAsc(List<Long> missionIds, ExpenseStatus status);

    List<Expense> findAllByMissionIdInAndExpenseReportStatusAndStatusOrderByExpenseDateAscIdAsc(List<Long> missionIds, ExpenseReportStatus reportStatus, ExpenseStatus status);

    @Query("select e from Expense e where e.mission.id in :missionIds and e.expenseReport.status = :reportStatus order by e.expenseDate asc, e.id asc")
    List<Expense> findAllByMissionIdInAndExpenseReportStatusOrderByExpenseDateAscIdAsc(@Param("missionIds") List<Long> missionIds, @Param("reportStatus") ExpenseReportStatus reportStatus);

    boolean existsByMissionIdAndProfileIdAndExpenseDateAndComment(Long missionId, Long profileId, LocalDate expenseDate, String comment);
}
