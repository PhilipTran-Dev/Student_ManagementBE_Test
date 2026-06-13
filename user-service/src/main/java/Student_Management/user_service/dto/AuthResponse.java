package Student_Management.user_service.dto;

import Student_Management.user_service.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String refreshToken;
    private Long id;
    private String email;
    private String fullName;
    private Role role;
}