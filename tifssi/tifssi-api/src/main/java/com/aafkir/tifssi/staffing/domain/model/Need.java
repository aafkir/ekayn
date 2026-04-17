package com.aafkir.tifssi.staffing.domain.model;

import com.aafkir.tifssi.crm.domain.model.Company;
import com.aafkir.tifssi.crm.domain.model.Contact;
import com.aafkir.tifssi.projects.domain.model.Project;
import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import com.aafkir.tifssi.staffing.domain.enums.NeedStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "need", schema = "staffing")
public class Need extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_need_company"))
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", foreignKey = @ForeignKey(name = "fk_need_contact"))
    private Contact contact;

    @Size(max = 50)
    @Column(name = "need_reference", length = 50)
    private String needReference;

    @NotBlank
    @Size(max = 150)
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Size(max = 4000)
    @Column(name = "description", length = 4000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "need_status", nullable = false, length = 20)
    private NeedStatus status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Size(max = 150)
    @Column(name = "location_name", length = 150)
    private String locationName;

    @Column(name = "remote_possible", nullable = false)
    private boolean remotePossible;

    @PositiveOrZero
    @Column(name = "target_daily_rate", precision = 12, scale = 2)
    private BigDecimal targetDailyRate;

    @OneToMany(mappedBy = "need", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NeedSkill> needSkills = new ArrayList<>();

    @OneToMany(mappedBy = "need", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Submission> submissions = new ArrayList<>();

    @OneToOne(mappedBy = "originNeed", fetch = FetchType.LAZY)
    private Project project;
}
