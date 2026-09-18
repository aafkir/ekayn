package com.aafkir.tifssi.shared.infrastructure.bootstrap;

import com.aafkir.tifssi.auth.domain.*;
import com.aafkir.tifssi.auth.infrastructure.UserAccountRepository;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import java.util.Locale;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;

@Component @org.springframework.context.annotation.Profile("dev") @ConditionalOnProperty(prefix="app.demo-data", name="enabled", havingValue="true") @Order(100)
public class AuthDevDataRunner implements ApplicationRunner {
    private final UserAccountRepository users; private final ProfileRepository profiles; private final PasswordEncoder encoder;
    public AuthDevDataRunner(UserAccountRepository users, ProfileRepository profiles, PasswordEncoder encoder){this.users=users;this.profiles=profiles;this.encoder=encoder;}
    @Override @Transactional public void run(ApplicationArguments args) {
        Profile nina = profiles.findByEmailAddress("nina.dupont@tifssi-dev.example").orElse(null);
        if (nina != null) upsert("nina.dupont@tifssi-dev.example", "Nina", "Dupont", Role.CONSULTANT, nina, "NinaDev!2026");
        Profile managerProfile = profiles.findAll().stream().filter(p -> nina == null || !p.getId().equals(nina.getId())).findFirst().orElse(null);
        upsert("manager@tifssi-dev.example", "Marc", "Manager", Role.MANAGER, managerProfile, "ManagerDev!2026");
        upsert("admin@tifssi-dev.example", "Ada", "Admin", Role.ADMIN, null, "AdminDev!2026");
    }
    private void upsert(String email, String first, String last, Role role, Profile profile, String password) {
        UserAccount u = users.findByEmailIgnoreCase(email).orElseGet(UserAccount::new); u.setEmail(email.toLowerCase(Locale.ROOT)); u.setFirstName(first); u.setLastName(last); u.setRole(role); u.setEnabled(true); u.setProfile(profile); if (u.getPasswordHash()==null) u.setPasswordHash(encoder.encode(password)); users.save(u);
    }
}
