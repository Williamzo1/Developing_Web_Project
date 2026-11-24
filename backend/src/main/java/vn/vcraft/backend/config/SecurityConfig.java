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
  SecurityFilterChain security(
          HttpSecurity http, AccountRepository accounts, SecurityErrorWriter errors) throws Exception {
    return http.cors(org.springframework.security.config.Customizer.withDefaults()).csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(
                    e ->
                            e.authenticationEntryPoint(
                                            (req, res, ex) ->
                                                    errors.write(req, res, 401, "UNAUTHENTICATED", "Vui lòng đăng nhập."))
                                    .accessDeniedHandler(
                                            (req, res, ex) ->
                                                    errors.write(
                                                            req,
                                                            res,
                                                            403,
                                                            "FORBIDDEN",
                                                            "Bạn không có quyền thực hiện thao tác.")))
            .authorizeHttpRequests(
                    auth ->
                            auth.requestMatchers(
                                            HttpMethod.GET,
                                            "/api/home", "/api/workshops/**", "/api/batches/**", "/api/news/**", "/media/**",
                                            "/api/health",
                                            "/api/health/**",
                                            "/api/products/**",
                                            "/api/quote-tags",
                                            "/api/quote-categories")
                                    .permitAll()
                                    .requestMatchers(HttpMethod.POST, "/api/login", "/api/register", "/api/register/verify-email", "/api/register/resend-otp")
                                    .permitAll()
                                    // Ảnh upload là tệp tĩnh công khai (FR-MEDIA-03).
                                    .requestMatchers(HttpMethod.GET, "/media/**")
                                    .permitAll()
                                    .requestMatchers(HttpMethod.HEAD, "/media/**")
                                    .permitAll()
                                    .requestMatchers("/error")
                                    .permitAll()
                                    .requestMatchers("/api/admin/**")
                                    .hasRole("ADMIN")
                                    .requestMatchers("/api/buyer/**")
                                    .hasRole("BUYER")
                                    .requestMatchers("/api/artisan/**")
                                    .hasRole("ARTISAN")
                                    .anyRequest()
                                    .authenticated())
            .addFilterBefore(
                    new BearerTokenFilter(accounts), UsernamePasswordAuthenticationFilter.class)
            .build();
  }
}
