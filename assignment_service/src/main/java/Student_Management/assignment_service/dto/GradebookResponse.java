// GradebookResponse.java
package Student_Management.assignment_service.dto;
import lombok.*;
import java.util.List;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class GradebookResponse {
    private List<GradebookAssignmentInfo> assignments;
    private List<GradebookStudentRow> studentRows;
}