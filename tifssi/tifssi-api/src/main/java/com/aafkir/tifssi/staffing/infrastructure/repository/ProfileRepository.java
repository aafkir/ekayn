package com.aafkir.tifssi.staffing.infrastructure.repository;

import com.aafkir.tifssi.staffing.domain.model.Profile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Profile p where p.id = :id")
    Optional<Profile> findForTimesheetCreation(@org.springframework.data.repository.query.Param("id") Long id);

    Optional<Profile> findByEmailAddress(String emailAddress);
}
