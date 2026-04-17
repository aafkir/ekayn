package com.aafkir.tifssi.staffing.domain.model;

import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "need_skill",
        schema = "staffing",
        uniqueConstraints = @UniqueConstraint(name = "uk_need_skill_need_skill", columnNames = {"need_id", "skill_id"})
)
public class NeedSkill extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "need_id", nullable = false, foreignKey = @ForeignKey(name = "fk_need_skill_need"))
    private Need need;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false, foreignKey = @ForeignKey(name = "fk_need_skill_skill"))
    private Skill skill;

    @Column(name = "required_level")
    private Integer requiredLevel;

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory = true;
}

