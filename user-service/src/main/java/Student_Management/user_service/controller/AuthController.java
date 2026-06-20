package Student_Management.user_service.controller;

import Student_Management.user_service.dto.AuthResponse;
import Student_Management.user_service.dto.LoginRequest;
import Student_Management.user_service.dto.RegisterRequest;
import Student_Management.user_service.dto.*;
import Student_Management.user_service.entity.Role;
import Student_Management.user_service.entity.User;
import Student_Management.user_service.service.AuthService;
import Student_Management.user_service.service.EmailOtpService;
import Student_Management.user_service.service.EmailOtpStorageService;
import Student_Management.user_service.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailOtpService emailOtpService;
    private final EmailOtpStorageService otpStorageService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Student endpoints
    @PostMapping("/student/register")
    public ResponseEntity<AuthResponse> registerStudent(@Valid @RequestBody RegisterRequest request) {
        request.setRole(Role.STUDENT);
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/student/login")
    public ResponseEntity<AuthResponse> loginStudent(@Valid @RequestBody LoginRequest request) {
        request.setRole("STUDENT");
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // Teacher endpoints
    @PostMapping("/teacher/login")
    public ResponseEntity<AuthResponse> loginTeacher(@Valid @RequestBody LoginRequest request) {
        request.setRole("TEACHER");
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // Admin endpoints
    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> loginAdmin(@Valid @RequestBody LoginRequest request) {
        request.setRole("ADMIN");
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // Shared endpoints
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        authService.logout(refreshToken);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ========== New endpoints for student OTP/password reset ==========

    /**
     * Initiate forgot password: send OTP if email exists and is a student.
     * Payload: { "email": "student@example.com" }
     */
    @PostMapping("/student/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        emailOtpService.sendOtp(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP dispatched successfully"));
    }

    /**
     * Verify entered OTP.
     * Payload: { "email": "...", "otp": "123456" }
     */
    @PostMapping("/student/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        var details = otpStorageService.getOtpDetails(request.getEmail());
        if (details == null || details.getExpireAt() == null || details.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP code has expired or is invalid.");
        }
        if (!details.getOtp().equals(request.getOtp())) {
            throw new BadCredentialsException("The entered OTP code is incorrect.");
        }

        // successful: remove OTP and generate a reset token valid for a short time
        otpStorageService.removeOtp(request.getEmail());
        String resetToken = otpStorageService.generateResetToken();
        otpStorageService.storeResetToken(request.getEmail(), resetToken);

        return ResponseEntity.ok(Map.of("message", "OTP verified successfully!", "resetToken", resetToken));
    }

    /**
     * Reset password after verification.
     * Payload: { "email": "...", "newPassword": "..." }
     */
    @PostMapping("/student/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordWithEmailRequest request) {
        String normalized = request.getEmail().trim().toLowerCase();
        // Validate reset token
        var tokenDetails = otpStorageService.getResetTokenDetails(normalized);
        if (tokenDetails == null || tokenDetails.getExpireAt() == null || tokenDetails.getExpireAt().isBefore(LocalDateTime.now())
                || !tokenDetails.getToken().equals(request.getResetToken())) {
            throw new IllegalArgumentException("Unauthorized password reset attempt. Token is invalid or expired.");
        }

        // token is valid -> remove it and update password
        otpStorageService.removeResetToken(normalized);

        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Email address not found in system"));

        if (user.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Password reset endpoint is for students only");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password has been successfully reset."));
    }
}