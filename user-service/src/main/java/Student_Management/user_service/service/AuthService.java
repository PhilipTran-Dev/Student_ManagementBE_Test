package Student_Management.user_service.service;

import Student_Management.user_service.dto.AuthResponse;
import Student_Management.user_service.dto.LoginRequest;
import Student_Management.user_service.dto.RegisterRequest;
import Student_Management.user_service.entity.RefreshToken;
import Student_Management.user_service.entity.User;
import Student_Management.user_service.entity.UserStatus;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Cross-portal validation: the endpoint-enforced role must match the user's database role
        if (request.getRole() != null
                && !request.getRole().equalsIgnoreCase(user.getRole().name())) {
            throw new IllegalArgumentException("Your account does not have access permissions for this portal.");
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
}