package com.aafkir.tifssi.auth.api;

import com.aafkir.tifssi.auth.api.AuthRequests.LoginRequest;
import com.aafkir.tifssi.auth.application.UserAccountService;
import com.aafkir.tifssi.auth.domain.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController @RequestMapping("/api/auth") @Tag(name="Authentication")
public class AuthController {
    private final AuthenticationManager manager; private final UserAccountService users;
    public AuthController(AuthenticationManager manager, UserAccountService users) { this.manager=manager; this.users=users; }
    @PostMapping("/login") public UserAccountResponse login(@Valid @RequestBody LoginRequest r, HttpServletRequest request) {
        try {
            Authentication auth = manager.authenticate(new UsernamePasswordAuthenticationToken(r.email().trim().toLowerCase(), r.password()));
            SecurityContextHolder.getContext().setAuthentication(auth);
            HttpSession session=request.getSession(true); session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
            return users.toResponse(users.byEmail(auth.getName()));
        } catch (AuthenticationException ex) { throw new BadCredentialsException("Invalid credentials"); }
    }
    @GetMapping("/me") public UserAccountResponse me(Authentication auth) { return users.toResponse(users.byEmail(auth.getName())); }
    @PostMapping("/logout") public ResponseEntity<Void> logout(HttpServletRequest request) throws Exception { if (request.getSession(false)!=null) request.getSession(false).invalidate(); SecurityContextHolder.clearContext(); return ResponseEntity.noContent().build(); }
}
