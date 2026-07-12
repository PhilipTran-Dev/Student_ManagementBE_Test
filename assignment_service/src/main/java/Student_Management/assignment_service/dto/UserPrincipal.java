package Student_Management.assignment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private final Long id;
    private final String email;
    private final String role;
}