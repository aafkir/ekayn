package com.aafkir.tifssi.timesheets.infrastructure.repository;

import com.aafkir.tifssi.timesheets.domain.model.TimeEntry;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

    List<TimeEntry> findAllByMissionIdOrderByWorkDateAscIdAsc(Long missionId);

    List<TimeEntry> findAllByProfileIdOrderByWorkDateAscIdAsc(Long profileId);

    List<TimeEntry> findAllByMissionIdAndWorkDateBetweenOrderByWorkDateAscIdAsc(Long missionId, LocalDate from, LocalDate to);

    List<TimeEntry> findAllByMissionIdInAndStatusOrderByWorkDateAscIdAsc(List<Long> missionIds, TimeEntryStatus status);
}
