package Student_Management.user_service.controller;

import Student_Management.user_service.dto.*;
import Student_Management.user_service.entity.Role;
import Student_Management.user_service.service.TeacherService;
import Student_Management.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllTeachers() {
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getTeacherById(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getTeacherById(id));
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        assertRole(Role.TEACHER);
        ProfileResponse profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        assertRole(Role.TEACHER);
        ProfileResponse updated = userService.updateCurrentUserProfile(request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/profile/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        assertRole(Role.TEACHER);
        Map<String, String> result = userService.changePassword(request);
        return ResponseEntity.ok(result);
    }

    private void assertRole(Role expected) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        boolean hasRole = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + expected.name()));
        if (!hasRole) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: insufficient permissions");
        }
    }
}