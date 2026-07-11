package Student_Management.class_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseResponse {
    private Long id;
    private String code;
    private String name;
    private Integer credits;
    private String level;
    private String facultyName;
}