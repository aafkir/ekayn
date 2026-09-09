package com.aafkir.tifssi.timesheets.domain.model;

import com.aafkir.tifssi.projects.domain.model.Mission;
import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryStatus;
import com.aafkir.tifssi.timesheets.domain.enums.TimeEntryUnitType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "time_entry", schema = "timesheets")
public class TimeEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id", foreignKey = @ForeignKey(name = "fk_time_entry_timesheet"))
    private Timesheet timesheet;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_time_entry_profile"))
    private Profile profile;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false, foreignKey = @ForeignKey(name = "fk_time_entry_mission"))
    private Mission mission;

    @NotNull
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @NotNull
    @Positive
    @Column(name = "quantity", nullable = false, precision = 6, scale = 2)
    private BigDecimal quantity;

    @Size(max = 1000)
    @Column(name = "comment", length = 1000)
    private String comment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, length = 20)
    private TimeEntryUnitType unitType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "time_entry_status", nullable = false, length = 20)
    private TimeEntryStatus status;
}
