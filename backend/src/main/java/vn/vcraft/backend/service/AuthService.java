package vn.vcraft.backend.service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.vcraft.backend.dto.LoginRequest;
import vn.vcraft.backend.dto.LoginResponse;
import vn.vcraft.backend.repository.AccountRepository;
import vn.vcraft.backend.security.BearerTokens;
import static org.springframework.http.HttpStatus.*;

@Service
public class AuthService {
    private final AccountRepository accounts;
    private final BCryptPasswordEncoder passwords;
    private final String dummyHash;

    public AuthService(AccountRepository accounts, BCryptPasswordEncoder passwords) {
        this.accounts = accounts;
        this.passwords = passwords;
        this.dummyHash = passwords.encode(UUID.randomUUID().toString());
    }

    public LoginResponse login(LoginRequest input) {
        if (input.username() == null || !input.username().matches("[a-z0-9-]{3,60}")
                || input.password() == null || input.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid credentials format");
        }
        var credentials = accounts.findActiveCredentials(input.username());
        String expected = credentials.map(AccountRepository.Credentials::passwordHash).orElse(dummyHash);
        if (!passwords.matches(input.password(), expected) || credentials.isEmpty()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Thông tin đăng nhập không hợp lệ");
        }
        var account = credentials.get();
        String token = UUID.randomUUID().toString() + UUID.randomUUID();
        accounts.createSession(account.id(), BearerTokens.hash(token));
        return new LoginResponse(token, account.role());
    }

    public void logout(String authorization) {
        BearerTokens.extract(authorization).ifPresent(token -> accounts.revokeSession(BearerTokens.hash(token)));
    }
}
