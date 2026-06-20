package Student_Management.user_service.service;

import Student_Management.user_service.dto.OtpDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory OTP storage service.
 */
@Service
@Slf4j
public class EmailOtpStorageService {

    private final Map<String, OtpDetails> otpStore = new ConcurrentHashMap<>();
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

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}

