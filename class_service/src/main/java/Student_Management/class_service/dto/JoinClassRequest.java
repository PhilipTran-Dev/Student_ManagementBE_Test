package Student_Management.class_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinClassRequest {
    @NotBlank(message = "Class code is required")
    private String code;

    private String password; // can be null if the class does not require a password
}