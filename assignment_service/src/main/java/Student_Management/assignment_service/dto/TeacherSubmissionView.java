package Student_Management.assignment_service.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TeacherSubmissionView {
    private Long studentId;
    private String studentName;
    private String studentCode; // student code from the user service
    private String className;
    private Long submissionId; // null if not submitted
    private List<String> fileUrls;
    private LocalDateTime submittedAt;
    private String status; // ON_TIME, LATE, MISSING
    private Double grade;
    private String feedback;
}