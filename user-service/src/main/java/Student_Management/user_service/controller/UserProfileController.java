package Student_Management.user_service.controller;

import Student_Management.user_service.dto.ChangePasswordRequest;
import Student_Management.user_service.dto.ProfileResponse;
import Student_Management.user_service.dto.ProfileUpdateRequest;
import Student_Management.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    /**
     * Get the authenticated user's profile.
     * No user ID is accepted from the path; the logged-in user is derived
     * from the Spring Security authentication context.
     */
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        ProfileResponse profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    /**
     * Update personal details for the authenticated user.
     * Only fullName, phoneNumber, dateOfBirth, and gender are mutable.
     * Academic fields (studentId, teacherId, faculty, major, className, email)
     * remain strictly read-only.
     */
    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        ProfileResponse updated = userService.updateCurrentUserProfile(request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Change the authenticated user's password.
     * Validates currentPassword against the stored hash,
     * ensures newPassword matches confirmPassword,
     * then encodes and persists the new password.
     */
    @PutMapping("/profile/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Map<String, String> result = userService.changePassword(request);
        return ResponseEntity.ok(result);
    }
}