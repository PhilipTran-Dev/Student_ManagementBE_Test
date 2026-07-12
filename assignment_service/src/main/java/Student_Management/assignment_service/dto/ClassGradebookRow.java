package Student_Management.assignment_service.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class ClassGradebookRow {
    private Long studentId;
    private String studentName;
    private String studentCode;
    private Map<Long, Double> assignmentGrades; // Key: assignmentId, Value: grade
    private Double totalAverage;
}