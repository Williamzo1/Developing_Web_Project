package vn.vcraft.backend.security;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.vcraft.backend.repository.AccountRepository;

public class BearerTokenFilter extends OncePerRequestFilter {
    private final AccountRepository accounts;

    public BearerTokenFilter(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        BearerTokens.extract(request.getHeader("Authorization")).map(BearerTokens::hash)
                .flatMap(accounts::findActiveSession)
                .ifPresent(actor -> SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(actor, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + actor.role())))));
        chain.doFilter(request, response);
    }
}
