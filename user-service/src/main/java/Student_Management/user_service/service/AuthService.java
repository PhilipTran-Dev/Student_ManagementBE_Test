package Student_Management.user_service.service;

import Student_Management.user_service.dto.AuthResponse;
import Student_Management.user_service.dto.LoginRequest;
import Student_Management.user_service.dto.RegisterRequest;
import Student_Management.user_service.entity.OtpCode;
import Student_Management.user_service.entity.RefreshToken;
import Student_Management.user_service.entity.User;
import Student_Management.user_service.entity.UserStatus;
import Student_Management.user_service.repository.OtpCodeRepository;
import Student_Management.user_service.repository.RefreshTokenRepository;
import Student_Management.user_service.repository.UserRepository;
import Student_Management.user_service.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Enforce strict cross-role check: the portal role enforced by the endpoint must match the database role
        if (request.getRole() != null
                && !request.getRole().equalsIgnoreCase(user.getRole().name())) {
            throw new BadCredentialsException("Access Denied: Invalid portal access permission.");
        }

        String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getRole(), user.getId());
        String newRefreshTokenValue = jwtUtils.generateRefreshToken(user.getEmail());

        // Upsert pattern: update existing token if present, otherwise create new
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(RefreshToken.builder().user(user).build());

        refreshToken.setToken(newRefreshTokenValue);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(newRefreshTokenValue)
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .studentId(request.getStudentId())
                .faculty(request.getFaculty())
                .major(request.getMajor())
                .className(request.getClassName())
                .build();
        user = userRepository.save(user);

        String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getRole(), user.getId());
        String newRefreshTokenValue = jwtUtils.generateRefreshToken(user.getEmail());

        // No prior token exists for a brand-new user, so always create
        RefreshToken refreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(newRefreshTokenValue)
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new IllegalArgumentException("Refresh token has expired. Please login again.");
        }

        User user = storedToken.getUser();

        // Rotate token in-place: update fields on the managed entity instead of delete+insert
        String newAccessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getRole(), user.getId());
        String newRefreshTokenValue = jwtUtils.generateRefreshToken(user.getEmail());

        storedToken.setToken(newRefreshTokenValue);
        storedToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(storedToken);

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    public void forgotPassword(String email) {
        // Basic placeholder implementation
        // Generates and stores an OTP for password reset
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        OtpCode otpCode = OtpCode.builder()
                .email(email)
                .code(otp)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .purpose("FORGOT_PASSWORD")
                .build();
        otpCodeRepository.save(otpCode);

        // In a real implementation, send OTP via email here
    }

    public boolean verifyOtp(String email, String otp) {
        return otpCodeRepository.findByEmailAndCodeAndPurpose(email, otp, "FORGOT_PASSWORD")
                .map(code -> code.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate all refresh tokens for security
        refreshTokenRepository.deleteByUser(user);
    }
}