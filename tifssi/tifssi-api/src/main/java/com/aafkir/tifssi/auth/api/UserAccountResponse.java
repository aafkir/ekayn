package com.aafkir.tifssi.auth.api;

import com.aafkir.tifssi.auth.domain.*;

public record UserAccountResponse(Long id, String email, String firstName, String lastName,
        Role role, boolean enabled, Long profileId) {}
