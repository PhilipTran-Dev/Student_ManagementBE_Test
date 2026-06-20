package Student_Management.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Simple holder for OTP details stored in-memory.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpDetails {
    private String email;
    private String otp;
    private LocalDateTime expireAt;
}

