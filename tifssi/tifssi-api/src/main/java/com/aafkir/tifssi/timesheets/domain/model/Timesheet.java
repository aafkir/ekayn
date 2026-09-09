package com.aafkir.tifssi.timesheets.domain.model;

import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.timesheets.domain.enums.TimesheetStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;

@Getter @Setter @NoArgsConstructor @Entity
@Table(name="timesheet", schema="timesheets", uniqueConstraints=@UniqueConstraint(name="uk_timesheet_profile_period", columnNames={"profile_id","year_number","month_number"}))
public class Timesheet extends BaseEntity {
    @NotNull @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="profile_id", nullable=false, foreignKey=@ForeignKey(name="fk_timesheet_profile")) private Profile profile;
    @Min(2000) @Column(name="year_number", nullable=false) private Integer year;
    @Min(1) @Max(12) @Column(name="month_number", nullable=false) private Integer month;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="status", nullable=false, length=20) private TimesheetStatus status=TimesheetStatus.DRAFT;
    @Column(name="submitted_at") private Instant submittedAt;
    @Column(name="validated_at") private Instant validatedAt;
    @Column(name="validated_by") private Long validatedBy;
    @Column(name="rejected_at") private Instant rejectedAt;
    @Column(name="rejected_by") private Long rejectedBy;
    @Size(max=2000) @Column(name="rejection_reason", length=2000) private String rejectionReason;
    @OneToMany(mappedBy="timesheet") private List<TimeEntry> timeEntries=new ArrayList<>();
}
