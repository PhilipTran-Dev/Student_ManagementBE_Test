package Student_Management.user_service.service;

import Student_Management.user_service.dto.UserResponse;
import Student_Management.user_service.entity.Role;
import Student_Management.user_service.entity.User;
import Student_Management.user_service.entity.UserStatus;
import Student_Management.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllStudents() {
        return userRepository.findByRole(Role.STUDENT).stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getStudentById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));
        if (user.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("User is not a student");
        }
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .status(user.getStatus())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .phoneNumber(user.getPhoneNumber())
                .studentId(user.getStudentId())
                .faculty(user.getFaculty())
                .major(user.getMajor())
                .className(user.getClassName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}