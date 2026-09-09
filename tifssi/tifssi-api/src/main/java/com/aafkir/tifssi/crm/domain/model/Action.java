package com.aafkir.tifssi.crm.domain.model;

import com.aafkir.tifssi.crm.domain.enums.ActionStatus;
import com.aafkir.tifssi.crm.domain.enums.ActionType;
import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "action", schema = "crm")
public class Action extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_action_company"))
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", foreignKey = @ForeignKey(name = "fk_action_contact"))
    private Contact contact;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private ActionType type;

    @NotBlank
    @Size(max = 150)
    @Column(name = "subject", nullable = false, length = 150)
    private String subject;

    @Size(max = 4000)
    @Column(name = "comment", length = 4000)
    private String comment;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action_status", nullable = false, length = 20)
    private ActionStatus status;

    @Size(max = 150)
    @Column(name = "responsible_name", length = 150)
    private String responsibleName;

    @Size(max = 1000)
    @Column(name = "next_step", length = 1000)
    private String nextStep;
}
