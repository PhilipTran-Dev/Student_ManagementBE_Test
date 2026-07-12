package Student_Management.assignment_service.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StudentAssignmentResponse {
    private Long id;
    private Long classId;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Double maxMark;
    private List<String> attachments;
    private List<String> submittedFiles;
    // Dynamic Fields
    private AssignmentState state; // TODO, DONE, MISSING
    private Double earnedGrade;
    private String feedback;
    private LocalDateTime submittedAt;
    private String submissionStatus; // ON_TIME or LATE
}