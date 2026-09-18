package com.aafkir.tifssi.staffing.application.service;

import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.auth.domain.UserAccount;
import com.aafkir.tifssi.auth.infrastructure.UserAccountRepository;
import com.aafkir.tifssi.staffing.api.dto.response.CurrentUserProfileResponse;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CurrentUserProfileService {
    private final ProfileRepository profiles;
    private final Environment environment;
    private final String devEmail;
    private final UserAccountRepository userAccounts;

    public CurrentUserProfileService(ProfileRepository profiles,
            Environment environment,
            @Value("${app.dev.current-user-email:}") String devEmail) {
        this(profiles, environment, devEmail, null);
    }

    @Autowired
    public CurrentUserProfileService(ProfileRepository profiles, Environment environment,
            @Value("${app.dev.current-user-email:}") String devEmail,
            UserAccountRepository userAccounts) {
        this.profiles = profiles;
        this.environment = environment;
        this.devEmail = devEmail;
        this.userAccounts = userAccounts;
    }

    public CurrentUserProfileResponse get(Principal principal) {
        if (principal == null && !environment.acceptsProfiles(Profiles.of("dev"))) {
            throw new ResourceNotFoundException("Current user", "not authenticated");
        }
        if (principal == null) {
            Profile profile = profiles.findByEmailAddress(devEmail.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Profile for current user", devEmail));
            return fromProfile(profile);
        }
        if (userAccounts != null) {
            UserAccount account = userAccounts.findByEmailIgnoreCase(principal.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User account", principal.getName()));
            Profile profile = account.getProfile();
            if (profile == null) throw new ResourceNotFoundException("Profile for current user", account.getEmail());
            return fromProfile(profile);
        }
        String identity = principal.getName();
        Profile profile = profiles.findByEmailAddress(identity.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Profile for current user", identity));
        return fromProfile(profile);
    }

    private CurrentUserProfileResponse fromProfile(Profile profile) {
        return new CurrentUserProfileResponse(profile.getEmailAddress(), profile.getId(), profile.getFirstName(), profile.getLastName(), profile.getJobTitle());
    }
}
