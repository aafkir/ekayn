package com.aafkir.tifssi.auth.infrastructure;

import com.aafkir.tifssi.auth.domain.UserAccount;
import java.util.Locale;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.web.cors.*;

@Configuration @EnableMethodSecurity
public class SecurityConfig {
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception { return c.getAuthenticationManager(); }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean UserDetailsService userDetailsService(UserAccountRepository repo) { return email -> {
        UserAccount u = repo.findByEmailIgnoreCase(email).orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        return User.withUsername(u.getEmail()).password(u.getPasswordHash()).disabled(!u.isEnabled())
                .authorities(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())).build();
    }; }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .cors(cors -> {})
            .sessionManagement(s -> s.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/auth/login", "/api/auth/logout", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health").permitAll()
                .requestMatchers("/api/auth/me", "/api/profiles/me").authenticated()
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/profiles", "/api/profiles/*").hasAnyRole("ADMIN", "MANAGER")
                .anyRequest().permitAll())
            .formLogin(f -> f.disable()).httpBasic(b -> b.disable())
            .logout(l -> l.logoutUrl("/api/auth/logout").invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll());
        return http.build();
    }
    @Bean CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration(); c.setAllowedOrigins(java.util.List.of("http://localhost:4200")); c.setAllowedMethods(java.util.List.of("GET","POST","PATCH","DELETE","OPTIONS")); c.setAllowedHeaders(java.util.List.of("Content-Type","X-XSRF-TOKEN")); c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource(); s.registerCorsConfiguration("/api/**", c); return s;
    }
}
