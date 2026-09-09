package com.aafkir.tifssi.absences.infrastructure.repository;

import com.aafkir.tifssi.absences.domain.model.Absence;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbsenceRepository extends JpaRepository<Absence, Long> {

    List<Absence> findAllByProfileIdOrderByStartDateAscIdAsc(Long profileId);

    boolean existsByProfileIdAndStartDateAndEndDateAndComment(Long profileId, LocalDate startDate, LocalDate endDate, String comment);
}
