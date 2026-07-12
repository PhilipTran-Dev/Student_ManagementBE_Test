package Student_Management.assignment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassMemberResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String studentId;
    private String className;
    private LocalDateTime joinedAt;
}