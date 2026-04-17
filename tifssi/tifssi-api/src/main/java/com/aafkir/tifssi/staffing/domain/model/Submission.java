package com.aafkir.tifssi.staffing.domain.model;

import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import com.aafkir.tifssi.staffing.domain.enums.SubmissionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "submission",
        schema = "staffing",
        uniqueConstraints = @UniqueConstraint(name = "uk_submission_need_profile", columnNames = {"need_id", "profile_id"})
)
public class Submission extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "need_id", nullable = false, foreignKey = @ForeignKey(name = "fk_submission_need"))
    private Need need;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_submission_profile"))
    private Profile profile;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "submission_status", nullable = false, length = 20)
    private SubmissionStatus status;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "proposed_daily_rate", precision = 12, scale = 2)
    private BigDecimal proposedDailyRate;

    @Size(max = 2000)
    @Column(name = "notes", length = 2000)
    private String notes;
}

