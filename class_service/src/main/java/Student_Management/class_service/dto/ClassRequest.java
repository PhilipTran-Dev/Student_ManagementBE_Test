package Student_Management.class_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassRequest {
    @NotBlank(message = "Class's name has not been blank")
    private String name;

    @NotNull(message = "Course's ID has not been blank")
    private Long courseId;

    @NotNull(message = "Semester's ID has not been blank")
    private Long semesterId;
}