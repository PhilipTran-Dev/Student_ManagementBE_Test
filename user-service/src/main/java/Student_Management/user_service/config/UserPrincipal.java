package Student_Management.user_service.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {

    private Long userId;
    private String email;
    private String role;
}