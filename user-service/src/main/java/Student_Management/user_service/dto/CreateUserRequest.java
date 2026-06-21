package Student_Management.user_service.dto;

import Student_Management.user_service.entity.Role;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @JsonProperty("full_name")
    private String fullName;

    @NotNull(message = "Role is required")
    private Role role;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;
    private String gender;
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("student_id")
    private String studentId;
    private String faculty;
    private String major;
    @JsonProperty("class_name")
    @JsonAlias("class")
    private String className;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    @JsonProperty("teacher_id")
    private String teacherId;
}