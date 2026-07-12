package Student_Management.assignment_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeRequest {
    @NotNull(message = "Grade is required")
    private Double grade;
    private String feedback;
}