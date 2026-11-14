package vn.vcraft.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.vcraft.backend.repository.AccountRepository;
import vn.vcraft.backend.security.BearerTokenFilter;
import vn.vcraft.backend.security.SecurityErrorWriter;

@Configuration
class SecurityConfig {
    @Bean
    BCryptPasswordEncoder passwords() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, AccountRepository accounts, SecurityErrorWriter errors) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) ->
                        errors.write(req, res, 401, "UNAUTHENTICATED", "Vui lòng đăng nhập."))
                    .accessDeniedHandler((req, res, ex) ->
                        errors.write(req, res, 403, "FORBIDDEN", "Bạn không có quyền thực hiện thao tác.")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/health", "/api/health/**", "/api/products/**",
                                "/api/categories", "/api/workshops", "/api/workshops/**", "/api/batches", "/api/batches/**",
                                "/api/news", "/api/news/**", "/api/home", "/api/map/workshops").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/buyer/**").hasRole("BUYER")
                        .requestMatchers("/api/artisan/**").hasRole("ARTISAN")
                        .anyRequest().authenticated())
                .addFilterBefore(new BearerTokenFilter(accounts), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
