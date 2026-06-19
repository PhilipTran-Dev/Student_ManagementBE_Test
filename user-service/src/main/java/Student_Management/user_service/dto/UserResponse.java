package Student_Management.user_service.dto;

import Student_Management.user_service.entity.Role;
import Student_Management.user_service.entity.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private UserStatus status;
    private String dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String studentId;
    private String faculty;
    private String major;
    private String className;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}