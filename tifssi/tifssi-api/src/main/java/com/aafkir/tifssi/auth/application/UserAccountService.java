package com.aafkir.tifssi.auth.application;

import com.aafkir.tifssi.auth.api.*;
import com.aafkir.tifssi.auth.api.AuthRequests.*;
import com.aafkir.tifssi.auth.domain.UserAccount;
import com.aafkir.tifssi.auth.infrastructure.UserAccountRepository;
import com.aafkir.tifssi.shared.application.exception.ResourceNotFoundException;
import com.aafkir.tifssi.staffing.domain.model.Profile;
import com.aafkir.tifssi.staffing.infrastructure.repository.ProfileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional
public class UserAccountService {
    private final UserAccountRepository users;
    private final ProfileRepository profiles;
    private final PasswordEncoder passwords;

    @Transactional(readOnly = true) public List<UserAccountResponse> findAll() { return users.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public UserAccount get(Long id) { return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User account", String.valueOf(id))); }
    public UserAccount create(CreateUserRequest r) {
        UserAccount u = new UserAccount(); u.setEmail(r.email()); u.normalizeEmail(); u.setFirstName(r.firstName().trim()); u.setLastName(r.lastName().trim());
        u.setRole(r.role()); u.setPasswordHash(passwords.encode(r.password())); u.setEnabled(true); u.setProfile(profile(r.profileId())); return users.save(u);
    }
    public UserAccount patch(Long id, PatchUserRequest r) {
        UserAccount u = get(id); if (r.email()!=null) { u.setEmail(r.email()); u.normalizeEmail(); } if (r.firstName()!=null) u.setFirstName(r.firstName());
        if (r.lastName()!=null) u.setLastName(r.lastName()); if (r.role()!=null) u.setRole(r.role()); if (r.profileId()!=null) u.setProfile(profile(r.profileId())); return u;
    }
    public UserAccount setEnabled(Long id, boolean enabled) { UserAccount u=get(id); u.setEnabled(enabled); return u; }
    public void resetPassword(Long id, String password) { get(id).setPasswordHash(passwords.encode(password)); }
    @Transactional(readOnly = true) public UserAccount byEmail(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User account", email)); }
    public UserAccountResponse toResponse(UserAccount u) { return new UserAccountResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), u.getRole(), u.isEnabled(), u.getProfile()==null?null:u.getProfile().getId()); }
    private Profile profile(Long id) { return id == null ? null : profiles.findById(id).orElseThrow(() -> new ResourceNotFoundException("Profile", String.valueOf(id))); }
}
