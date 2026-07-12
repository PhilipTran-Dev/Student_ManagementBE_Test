package Student_Management.assignment_service.dto;
import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class StudentStatsResponse {
    private Double currentGpa;
    private Double gpaPercentage;
    private Long gradedTasks;
    private Long pendingTasks;
    private String academicStanding;
}