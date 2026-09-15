package com.aafkir.tifssi.expenses.domain.model;

import com.aafkir.tifssi.expenses.domain.enums.ExpenseCategory;
import com.aafkir.tifssi.expenses.domain.enums.ExpenseStatus;
import com.aafkir.tifssi.projects.domain.model.Mission;
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
@Table(name = "expense", schema = "expenses")
public class Expense extends BaseEntity {

    @NotNull @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="expense_report_id", nullable=false, foreignKey=@ForeignKey(name="fk_expense_report"))
    private ExpenseReport expenseReport;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_profile"))
    private Profile profile;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expense_mission"))
    private Mission mission;

    @NotNull
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "expense_category", nullable = false, length = 30)
    private ExpenseCategory category;

    @Size(max = 1000)
    @Column(name = "comment", length = 1000)
    private String comment;

    @Positive
    @NotNull
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Size(max = 3)
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currency;

    @Size(max = 500)
    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(name = "billable", nullable = false)
    private boolean billable;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "expense_status", nullable = false, length = 20)
    private ExpenseStatus status;
}
