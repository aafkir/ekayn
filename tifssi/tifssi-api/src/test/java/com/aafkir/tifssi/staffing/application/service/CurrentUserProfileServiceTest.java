package com.aafkir.tifssi.staffing.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import java.security.Principal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

class CurrentUserProfileServiceTest {
    @Test
    void mapsAuthenticatedIdentityToProfile() {
        ProfileRepository repository = Mockito.mock(ProfileRepository.class);
        Environment environment = Mockito.mock(Environment.class);
        when(environment.acceptsProfiles(org.springframework.core.env.Profiles.of("dev"))).thenReturn(false);
        Profile profile = new Profile();
        profile.setId(7L); profile.setFirstName("Nina"); profile.setLastName("Dupont"); profile.setEmailAddress("nina@example.com"); profile.setJobTitle("Consultante");
        when(repository.findByEmailAddress("nina@example.com")).thenReturn(Optional.of(profile));
        Principal principal = () -> "nina@example.com";
        var response = new CurrentUserProfileService(repository, environment, "").get(principal);
        assertThat(response).extracting("userId", "profileId", "firstName", "lastName", "role")
                .containsExactly("nina@example.com", 7L, "Nina", "Dupont", "Consultante");
    }

    @Test
    void usesConfiguredDevFallbackWithoutPrincipal() {
        ProfileRepository repository = Mockito.mock(ProfileRepository.class);
        Environment environment = Mockito.mock(Environment.class);
        when(environment.acceptsProfiles(org.springframework.core.env.Profiles.of("dev"))).thenReturn(true);
        Profile profile = new Profile();
        profile.setId(7L); profile.setFirstName("Nina"); profile.setLastName("Dupont"); profile.setEmailAddress("nina@example.com");
        when(repository.findByEmailAddress("nina@example.com")).thenReturn(Optional.of(profile));
        var response = new CurrentUserProfileService(repository, environment, "nina@example.com").get(null);
        assertThat(response.profileId()).isEqualTo(7L);
    }
}
