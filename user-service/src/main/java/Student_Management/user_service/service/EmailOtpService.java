package Student_Management.user_service.service;

import Student_Management.user_service.entity.Role;
import Student_Management.user_service.entity.User;
import Student_Management.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service responsible for sending OTP emails to students.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailOtpService {

    private final UserRepository userRepository;
    private final EmailOtpStorageService otpStorageService;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Validate user and role, generate OTP and send email.
     */
    public void sendOtp(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        String normalized = email.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalized);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Email address not found in system");
        }
        User user = userOpt.get();

        if (user.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("OTP workflow allowed only for students");
        }

        // Generate and store OTP
        String otp = otpStorageService.generateOtp();
        otpStorageService.storeOtp(normalized, otp);

        // Build mail
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(normalized);
        message.setSubject("CTUT Account Verification OTP");
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(user.getFullName() != null ? user.getFullName() : "Student").append(",\n\n");
        sb.append("Your CTUT account verification OTP code is: ").append(otp).append("\n");
        sb.append("This code is valid for 5 minutes. If you did not request this, please ignore this email.\n\n");
        sb.append("Regards,\nCTUT Support Team");
        message.setText(sb.toString());

        // Send (may throw MailException which will propagate)
        javaMailSender.send(message);
        log.info("Sent OTP email to {}", normalized);
    }
}

