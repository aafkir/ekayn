package com.aafkir.tifssi.staffing.infrastructure.repository;

import com.aafkir.tifssi.staffing.domain.model.Submission;
import com.aafkir.tifssi.staffing.domain.enums.SubmissionStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findAllByNeedIdOrderByIdAsc(Long needId);

    List<Submission> findAllByProfileIdOrderByIdAsc(Long profileId);

    boolean existsByNeedIdAndProfileId(Long needId, Long profileId);

    boolean existsByNeedIdAndStatus(Long needId, SubmissionStatus status);

    boolean existsByNeedIdAndStatusAndIdNot(Long needId, SubmissionStatus status, Long id);
}
