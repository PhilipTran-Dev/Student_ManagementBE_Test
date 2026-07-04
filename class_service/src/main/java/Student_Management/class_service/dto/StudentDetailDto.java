package Student_Management.class_service.dto;

import lombok.Data;

@Data
public class StudentDetailDto {
    private Long id;
    private String email;
    private String fullName;
    private String studentId;
    private String className;
}