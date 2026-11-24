package vn.vcraft.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vcraft.backend.exception.ApiException;

import vn.vcraft.backend.repository.OtpRepository;
import org.springframework.mail.MailException;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
public class OtpService {

    private final JavaMailSender mailer;
    private final OtpRepository otpRepo;  // JDBC repository — xem 1.4

    @Value("${auth.otp-minutes:15}")
    private int otpMinutes;

    public OtpService(JavaMailSender mailer, OtpRepository otpRepo) {
        this.mailer = mailer;
        this.otpRepo = otpRepo;
    }

    /** Sinh OTP 6 chữ số, lưu hash vào DB, gửi email. */
    @Transactional
    public void sendEmailOtp(long accountId, String toEmail, String purpose) {
        // Hủy OTP cũ chưa dùng của cùng account + purpose
        otpRepo.invalidateExisting(accountId, purpose);

        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        String hash = sha256(otp);
        Instant expiresAt = Instant.now().plus(otpMinutes, ChronoUnit.MINUTES);

        otpRepo.create(accountId, hash, purpose, expiresAt);

        // Gửi email (async trong production — đơn giản hóa cho bài tập)
        var msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("[vCraft] Mã xác thực email");
        msg.setText("Mã xác thực của bạn là: " + otp +
                "\nMã có hiệu lực trong " + otpMinutes + " phút." +
                "\nNếu bạn không yêu cầu, hãy bỏ qua email này.");
        try {
            mailer.send(msg);
        } catch (MailException ex) {
            LoggerFactory.getLogger(OtpService.class).warn("Không gửi được email xác thực; có thể yêu cầu gửi lại.");
        }
    }

    /** Xác thực OTP; đánh dấu đã dùng. Ném ApiException nếu sai/hết hạn. */
    @Transactional
    public void verifyOtp(long accountId, String otp, String purpose) {
        String hash = sha256(otp);
        boolean valid = otpRepo.markUsed(accountId, hash, purpose, Instant.now());
        if (!valid) {
            throw new ApiException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "INVALID_OTP",
                    "Mã xác thực không đúng hoặc đã hết hạn.");
        }
    }

    private static String sha256(String input) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
