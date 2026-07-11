package Student_Management.class_service.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class SemesterResponse {
    private Long id;
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}