package com.aafkir.tifssi.staffing.infrastructure.repository;

import com.aafkir.tifssi.staffing.domain.model.ProfileSkill;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileSkillRepository extends JpaRepository<ProfileSkill, Long> {

    @Query("""
            select profileSkill
            from ProfileSkill profileSkill
            join fetch profileSkill.skill
            where profileSkill.profile.id = :profileId
            order by profileSkill.primarySkill desc, profileSkill.skill.skillName asc
            """)
    List<ProfileSkill> findAllByProfileIdWithSkill(@Param("profileId") Long profileId);
}
