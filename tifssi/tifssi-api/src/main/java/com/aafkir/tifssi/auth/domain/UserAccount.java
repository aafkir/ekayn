package com.aafkir.tifssi.auth.domain;

import com.aafkir.tifssi.shared.domain.model.BaseEntity;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import jakarta.persistence.*;
import java.util.Locale;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "user_account", schema = "shared",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_account_email", columnNames = "email"))
public class UserAccount extends BaseEntity {
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    @Column(name = "first_name", nullable = false, length = 100) private String firstName;
    @Column(name = "last_name", nullable = false, length = 100) private String lastName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Role role;
    @Column(nullable = false) private boolean enabled = true;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id") private Profile profile;

    public void normalizeEmail() { if (email != null) email = email.trim().toLowerCase(Locale.ROOT); }
}
