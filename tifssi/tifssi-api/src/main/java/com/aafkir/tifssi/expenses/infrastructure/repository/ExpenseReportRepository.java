package com.aafkir.tifssi.expenses.infrastructure.repository;

import com.aafkir.tifssi.expenses.domain.model.ExpenseReport;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseReportStatus;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ExpenseReportRepository extends JpaRepository<ExpenseReport, Long> {
    @Query("select distinct r from ExpenseReport r left join fetch r.expenses e where (:profileId is null or r.profile.id=:profileId) and (:year is null or r.year=:year) and (:month is null or r.month=:month) and (:status is null or r.status=:status) order by r.year desc,r.month desc,r.profile.id asc")
    List<ExpenseReport> findAllByFilters(@Param("profileId") Long profileId,@Param("year") Integer year,@Param("month") Integer month,@Param("status") ExpenseReportStatus status);
    @Query("select r from ExpenseReport r left join fetch r.expenses where r.id=:id")
    Optional<ExpenseReport> findDetailedById(@Param("id") Long id);
    Optional<ExpenseReport> findByProfileIdAndYearAndMonth(Long profileId,Integer year,Integer month);
}
