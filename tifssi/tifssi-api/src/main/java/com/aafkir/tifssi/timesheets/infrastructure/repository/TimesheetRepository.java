package com.aafkir.tifssi.timesheets.infrastructure.repository;

import com.aafkir.tifssi.timesheets.domain.model.Timesheet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
    @org.springframework.data.jpa.repository.Query("""
            select t from Timesheet t
            where (:profileId is null or t.profile.id = :profileId)
              and (:year is null or t.year = :year)
              and (:month is null or t.month = :month)
            order by t.year desc, t.month desc, t.profile.id asc
            """)
    List<Timesheet> findAllByFilters(@org.springframework.data.repository.query.Param("profileId") Long profileId,
                                    @org.springframework.data.repository.query.Param("year") Integer year,
                                    @org.springframework.data.repository.query.Param("month") Integer month);

    List<Timesheet> findAllByProfileIdAndYearAndMonth(Long profileId, Integer year, Integer month);
    List<Timesheet> findAllByProfileId(Long profileId);
    Optional<Timesheet> findByProfileIdAndYearAndMonth(Long profileId, Integer year, Integer month);
}
