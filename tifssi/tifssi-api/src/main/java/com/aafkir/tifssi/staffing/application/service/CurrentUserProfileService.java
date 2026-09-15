package com.aafkir.tifssi.staffing.application.service;

import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.staffing.api.dto.response.CurrentUserProfileResponse;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CurrentUserProfileService {
    private final ProfileRepository profiles;
    private final Environment environment;
    private final String devEmail;

    public CurrentUserProfileService(ProfileRepository profiles,
            Environment environment,
            @Value("${app.dev.current-user-email:}") String devEmail) {
        this.profiles = profiles;
        this.environment = environment;
        this.devEmail = devEmail;
    }

    public CurrentUserProfileResponse get(Principal principal) {
        String identity = principal == null && environment.acceptsProfiles(Profiles.of("dev")) ? devEmail
                : principal == null ? null : principal.getName();
        if (identity == null || identity.isBlank()) {
            throw new ResourceNotFoundException("Current user", "not authenticated");
        }
        Profile profile = profiles.findByEmailAddress(identity.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Profile for current user", identity));
        return new CurrentUserProfileResponse(identity, profile.getId(), profile.getFirstName(), profile.getLastName(), profile.getJobTitle());
    }
}
