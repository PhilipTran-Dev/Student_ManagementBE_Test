package Student_Management.class_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnnouncementRequest {
    @NotBlank(message = "Title of announcement must not be blank")
    private String title;

    @NotBlank(message = "Content of announcement must not be blank")
    private String content;
}