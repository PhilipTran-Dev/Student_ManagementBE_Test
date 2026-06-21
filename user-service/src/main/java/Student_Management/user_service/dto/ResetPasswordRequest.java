package Student_Management.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {

    @NotBlank(message = "Temporary password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty("new_password")
    private String newPassword;
}
