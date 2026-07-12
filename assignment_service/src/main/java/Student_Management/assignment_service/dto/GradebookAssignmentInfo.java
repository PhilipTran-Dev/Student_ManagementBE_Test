package Student_Management.assignment_service.dto;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradebookAssignmentInfo {
    private Long id;
    private String title;
    private Double maxMark;
}