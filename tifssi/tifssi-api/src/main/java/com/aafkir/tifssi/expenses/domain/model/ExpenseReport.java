package com.aafkir.tifssi.expenses.domain.model;

import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseReportStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;

@Getter @Setter @NoArgsConstructor @Entity
@Table(name="expense_report", schema="expenses", uniqueConstraints=@UniqueConstraint(name="uk_expense_report_profile_period", columnNames={"profile_id","year_number","month_number"}))
public class ExpenseReport extends BaseEntity {
    @NotNull @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="profile_id", nullable=false, foreignKey=@ForeignKey(name="fk_expense_report_profile")) private Profile profile;
    @Version @Column(name="version", nullable=false) private long version;
    @NotNull @Min(2000) @Column(name="year_number", nullable=false) private Integer year;
    @NotNull @Min(1) @Max(12) @Column(name="month_number", nullable=false) private Integer month;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="status", nullable=false, length=20) private ExpenseReportStatus status=ExpenseReportStatus.DRAFT;
    @Column(name="submitted_at") private Instant submittedAt;
    @Column(name="validated_at") private Instant validatedAt;
    @Column(name="validated_by") private Long validatedBy;
    @Column(name="rejected_at") private Instant rejectedAt;
    @Column(name="rejected_by") private Long rejectedBy;
    @Size(max=2000) @Column(name="rejection_reason", length=2000) private String rejectionReason;
    @OneToMany(mappedBy="expenseReport") @OrderBy("expenseDate ASC, id ASC") private List<Expense> expenses=new ArrayList<>();
}
