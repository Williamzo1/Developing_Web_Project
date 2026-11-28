package vn.vcraft.backend.service;

import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.exception.ApiException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.vcraft.backend.dto.LoginRequest;
import vn.vcraft.backend.dto.LoginResponse;
import vn.vcraft.backend.repository.AccountRepository;
import vn.vcraft.backend.security.BearerTokens;
import static org.springframework.http.HttpStatus.*;

@Service
@Validated
public class AuthService {
    private final AccountRepository accounts;
    private final BCryptPasswordEncoder passwords;
    private final String dummyHash;
    private final OtpService otpService;

    public AuthService(AccountRepository accounts, BCryptPasswordEncoder passwords, OtpService otpService) {
        this.accounts = accounts;
        this.otpService = otpService;
        this.passwords = passwords;
        this.dummyHash = passwords.encode(UUID.randomUUID().toString());
    }

    public LoginResponse login(@NotNull @Valid LoginRequest input) {
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

    // Thêm vào AuthService hiện tại (giữ login/logout cũ)

    @Transactional
    public RegisterResponse register(RegisterRequest input) {
        if (accounts.existsByUsername(input.username())) {
            throw new ApiException(CONFLICT, "DATA_CONFLICT",
                    "Tên đăng nhập đã được sử dụng.");
        }
        if (accounts.existsByEmail(input.email())) {
            throw new ApiException(CONFLICT, "DATA_CONFLICT",
                    "Email đã được đăng ký.");
        }

        String hash = passwords.encode(input.password());
        long accountId = accounts.createBuyer(
                input.username(), hash,
                input.displayName(), input.email(), input.phone());

        // Gửi OTP xác thực email
        otpService.sendEmailOtp(accountId, input.email(), "EMAIL_VERIFY");

        return new RegisterResponse(accountId, input.username(), input.displayName(),
                input.email(), false,
                "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.");
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest input) {
        var info = accounts.findEmailInfo(input.username())
                .orElseThrow(() -> new ApiException(NOT_FOUND, "NOT_FOUND",
                        "Không tìm thấy tài khoản."));

        if (info.emailVerified()) {
            throw new ApiException(BAD_REQUEST, "ALREADY_VERIFIED",
                    "Email đã được xác thực trước đó.");
        }

        otpService.verifyOtp(info.id(), input.otp(), "EMAIL_VERIFY");
        accounts.markEmailVerified(info.id());
    }

    @Transactional
    public void resendOtp(ResendOtpRequest input) {
        var info = accounts.findEmailInfo(input.username())
                .orElseThrow(() -> new ApiException(NOT_FOUND, "NOT_FOUND",
                        "Không tìm thấy tài khoản."));
        if (info.emailVerified()) {
            throw new ApiException(BAD_REQUEST, "ALREADY_VERIFIED",
                    "Email đã được xác thực.");
        }
        otpService.sendEmailOtp(info.id(), info.email(), "EMAIL_VERIFY");
    }
}
