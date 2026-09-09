package com.aafkir.tifssi.timesheets.infrastructure.repository;

import com.aafkir.tifssi.timesheets.domain.model.TimeEntry;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

    @Query("""
            select entry from TimeEntry entry
            where (:missionId is null or entry.mission.id = :missionId)
              and (:profileId is null or entry.profile.id = :profileId)
            order by entry.workDate asc, entry.id asc
            """)
    List<TimeEntry> findAllByFilters(@Param("missionId") Long missionId, @Param("profileId") Long profileId);

    List<TimeEntry> findAllByMissionIdAndWorkDateBetweenOrderByWorkDateAscIdAsc(Long missionId, LocalDate from, LocalDate to);

    List<TimeEntry> findAllByMissionIdInAndStatusOrderByWorkDateAscIdAsc(List<Long> missionIds, TimeEntryStatus status);

    boolean existsByMissionIdAndProfileIdAndWorkDateAndComment(Long missionId, Long profileId, LocalDate workDate, String comment);
}
