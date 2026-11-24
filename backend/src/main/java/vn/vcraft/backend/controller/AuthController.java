package vn.vcraft.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.dto.LoginResponse;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.AuthService;

@RestController
@RequestMapping("/api")
class AuthController {
    private final AuthService auth;

    AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    LoginResponse login(@Valid @RequestBody LoginRequest input) {
        return auth.login(input);
    }

    @GetMapping("/me")
    Actor me(@AuthenticationPrincipal Actor actor) {
        return actor;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@RequestHeader("Authorization") String authorization) {
        auth.logout(authorization);
    }
// Thêm vào AuthController.java (giữ /login, /me, /logout cũ)

@PostMapping("/register")
@ResponseStatus(HttpStatus.CREATED)
RegisterResponse register(@Valid @RequestBody RegisterRequest input) {
    return auth.register(input);
}

@PostMapping("/register/verify-email")
@ResponseStatus(HttpStatus.OK)
void verifyEmail(@Valid @RequestBody VerifyEmailRequest input) {
    auth.verifyEmail(input);
}

@PostMapping("/register/resend-otp")
@ResponseStatus(HttpStatus.OK)
void resendOtp(@Valid @RequestBody ResendOtpRequest input) {
    auth.resendOtp(input);
}

}
