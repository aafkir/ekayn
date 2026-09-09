package com.aafkir.tifssi.staffing.infrastructure.repository;

import com.aafkir.tifssi.staffing.domain.model.Profile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByEmailAddress(String emailAddress);
}
