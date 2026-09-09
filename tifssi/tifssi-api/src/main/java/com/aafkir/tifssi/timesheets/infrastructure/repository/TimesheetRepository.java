package com.aafkir.tifssi.timesheets.infrastructure.repository;

import com.aafkir.tifssi.timesheets.domain.model.Timesheet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
    List<Timesheet> findAllByProfileIdAndYearAndMonth(Long profileId, Integer year, Integer month);
    List<Timesheet> findAllByProfileId(Long profileId);
    Optional<Timesheet> findByProfileIdAndYearAndMonth(Long profileId, Integer year, Integer month);
}
