package com.aafkir.tifssi.staffing.domain.model;

import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "skill", schema = "staffing")
public class Skill extends BaseEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "skill_code", nullable = false, length = 50)
    private String skillCode;

    @NotBlank
    @Size(max = 150)
    @Column(name = "skill_name", nullable = false, length = 150)
    private String skillName;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfileSkill> profileSkills = new ArrayList<>();

    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NeedSkill> needSkills = new ArrayList<>();
}

