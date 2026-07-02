package Student_Management.class_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassFileResponse {
    private Long id;
    private Long classId;
    private String fileName;
    private Long fileSize;
    private Long uploadedBy;
    private LocalDateTime createdAt;
}