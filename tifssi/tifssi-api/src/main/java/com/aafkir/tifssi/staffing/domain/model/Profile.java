package com.aafkir.tifssi.staffing.domain.model;

import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import com.aafkir.tifssi.staffing.domain.enums.ProfileType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "profile", schema = "staffing")
public class Profile extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", nullable = false, length = 20)
    private ProfileType type;

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(name = "email_address", nullable = false, length = 150)
    private String emailAddress;

    @Size(max = 50)
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Size(max = 150)
    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Size(max = 100)
    @Column(name = "seniority_label", length = 100)
    private String seniorityLabel;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @PositiveOrZero
    @Column(name = "default_daily_rate", precision = 12, scale = 2)
    private BigDecimal defaultDailyRate;

    @Column(name = "availability_date")
    private LocalDate availabilityDate;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfileSkill> profileSkills = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Submission> submissions = new ArrayList<>();
}
