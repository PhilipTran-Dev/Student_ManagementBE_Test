package Student_Management.class_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class SemesterRequest {
    @NotBlank(message = "Semester code must not be blank")
    private String code;

    @NotBlank(message = "Semester name must not be blank")
    private String name;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Status must not be blank")
    private String status;
}