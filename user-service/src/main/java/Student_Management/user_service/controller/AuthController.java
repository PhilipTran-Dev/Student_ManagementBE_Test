package Student_Management.user_service.controller;

import Student_Management.user_service.dto.AuthResponse;
import Student_Management.user_service.dto.LoginRequest;
import Student_Management.user_service.dto.RegisterRequest;
import Student_Management.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Student endpoints
    @PostMapping("/student/register")
    public ResponseEntity<AuthResponse> registerStudent(@Valid @RequestBody RegisterRequest request) {
        request.setRole(Student_Management.user_service.entity.Role.STUDENT);
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
}