package Student_Management.assignment_service.dto;
import lombok.*;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradebookStudentRow {
    private Long studentId;
    private String studentName;
    private String studentCode;
    private Map<Long, Double> grades;
}