package com.aafkir.tifssi.auth.infrastructure;

import com.aafkir.tifssi.auth.domain.UserAccount;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
