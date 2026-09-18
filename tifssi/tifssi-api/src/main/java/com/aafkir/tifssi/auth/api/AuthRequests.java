package com.aafkir.tifssi.auth.api;

import com.aafkir.tifssi.auth.domain.Role;
import jakarta.validation.constraints.*;

public final class AuthRequests {
    private AuthRequests() {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record CreateUserRequest(@NotBlank @Email String email, @NotBlank @Size(max=100) String firstName,
            @NotBlank @Size(max=100) String lastName, @NotNull Role role, Long profileId,
            @NotBlank @Size(min=12, max=200) String password) {}
    public record PatchUserRequest(@Email String email, String firstName, String lastName, Role role, Long profileId) {}
    public record PasswordRequest(@NotBlank @Size(min=12, max=200) String password) {}
    public record EnabledRequest(@NotNull Boolean enabled) {}
}
