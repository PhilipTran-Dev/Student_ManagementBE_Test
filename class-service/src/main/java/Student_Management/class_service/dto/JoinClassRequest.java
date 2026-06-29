package Student_Management.class_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinClassRequest {

    @NotBlank(message = "Class code is required")
    @Size(min = 6, max = 6, message = "Class code must be exactly 6 characters")
    private String code;
}