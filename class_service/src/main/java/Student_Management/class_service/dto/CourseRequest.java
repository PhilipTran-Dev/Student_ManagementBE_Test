package Student_Management.class_service.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank(message = "Course code must not be blank")
    private String code;

    @NotBlank(message = "Course name must not be blank")
    private String name;

    @NotNull(message = "Credits is required")
    @Min(value = 1, message = "Credits must be at least 1")
    private Integer credits;

    private String level;
    private String facultyName;
}