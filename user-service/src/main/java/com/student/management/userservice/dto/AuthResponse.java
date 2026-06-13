package com.student.management.userservice.dto;

import com.student.management.userservice.entity.Role;
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