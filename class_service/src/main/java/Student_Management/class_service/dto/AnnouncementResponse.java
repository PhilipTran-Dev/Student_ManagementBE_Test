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
public class AnnouncementResponse {
    private Long id;
    private Long classId;
    private String title;
    private String content;
    private Long authorId;
    private String authorName; //teacher's name take from webclient from user-service
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}