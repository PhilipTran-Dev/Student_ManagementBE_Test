package Student_Management.user_service.service;

import Student_Management.user_service.dto.OtpDetails;
import Student_Management.user_service.dto.ResetTokenDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory OTP storage service.
 */
@Service
@Slf4j
public class EmailOtpStorageService {

    private final Map<String, OtpDetails> otpStore = new ConcurrentHashMap<>();
    private final Map<String, ResetTokenDetails> resetTokenStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a secure 6-digit OTP as zero-padded string.
     */
    public String generateOtp() {
        int number = secureRandom.nextInt(1_000_000); // 0 .. 999999
        return String.format("%06d", number);
    }

    /**
     * Store OTP for an email with 5 minute expiration.
     */
    public void storeOtp(String email, String otp) {
        if (email == null) return;
        String key = normalize(email);
        OtpDetails details = OtpDetails.builder()
                .email(key)
                .otp(otp)
                .expireAt(LocalDateTime.now().plusMinutes(5))
                .build();
        otpStore.put(key, details);
        log.debug("Stored OTP for {} expiring at {}", key, details.getExpireAt());
    }

    public OtpDetails getOtpDetails(String email) {
        if (email == null) return null;
        return otpStore.get(normalize(email));
    }

    public void removeOtp(String email) {
        if (email == null) return;
        otpStore.remove(normalize(email));
    }

    // ----- Reset token methods -----

    /**
     * Generate a random UUID token.
     */
    public String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Store a reset token for the email with 3 minute expiration.
     */
    public void storeResetToken(String email, String token) {
        if (email == null || token == null) return;
        String key = normalize(email);
        ResetTokenDetails details = ResetTokenDetails.builder()
                .email(key)
                .token(token)
                .expireAt(LocalDateTime.now().plusMinutes(3))
                .build();
        resetTokenStore.put(key, details);
        log.debug("Stored reset token for {} expiring at {}", key, details.getExpireAt());
    }

    public ResetTokenDetails getResetTokenDetails(String email) {
        if (email == null) return null;
        return resetTokenStore.get(normalize(email));
    }

    public void removeResetToken(String email) {
        if (email == null) return;
        resetTokenStore.remove(normalize(email));
    }

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}

