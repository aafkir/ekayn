package com.aafkir.tifssi.timesheets.domain.model;

import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.timesheets.domain.enums.TimesheetStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "timesheet", schema = "timesheets", uniqueConstraints = @UniqueConstraint(
        name = "uk_timesheet_profile_period", columnNames = {"profile_id", "year_number", "month_number"}))
public class Timesheet extends BaseEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_timesheet_profile"))
    private Profile profile;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @NotNull
    @Min(2000)
    @Column(name = "year_number", nullable = false)
    private Integer year;

    @NotNull
    @Min(1)
    @Max(12)
    @Column(name = "month_number", nullable = false)
    private Integer month;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TimesheetStatus status = TimesheetStatus.DRAFT;

    @Column(name = "submitted_at")
    private Instant submittedAt;
    @Column(name = "validated_at")
    private Instant validatedAt;
    @Column(name = "validated_by")
    private Long validatedBy;
    @Column(name = "rejected_at")
    private Instant rejectedAt;
    @Column(name = "rejected_by")
    private Long rejectedBy;

    @Size(max = 2000)
    @Column(name = "rejection_reason", length = 2000)
    private String rejectionReason;

    @OneToMany(mappedBy = "timesheet")
    @OrderBy("workDate ASC, id ASC")
    private List<TimeEntry> timeEntries = new ArrayList<>();
}
