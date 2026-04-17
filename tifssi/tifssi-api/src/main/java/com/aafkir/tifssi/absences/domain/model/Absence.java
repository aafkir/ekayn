package com.aafkir.tifssi.absences.domain.model;

import com.aafkir.tifssi.absences.domain.enums.AbsenceStatus;
import com.aafkir.tifssi.absences.domain.enums.AbsenceType;
import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "absence", schema = "absences")
public class Absence extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_absence_profile"))
    private Profile profile;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "absence_type", nullable = false, length = 20)
    private AbsenceType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "absence_status", nullable = false, length = 20)
    private AbsenceStatus status;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull
    @Positive
    @Column(name = "quantity", nullable = false, precision = 6, scale = 2)
    private BigDecimal quantity;

    @Size(max = 1000)
    @Column(name = "comment", length = 1000)
    private String comment;
}
