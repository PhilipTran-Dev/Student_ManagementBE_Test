package Student_Management.class_service.controller;

import Student_Management.class_service.dto.AnnouncementRequest;
import Student_Management.class_service.dto.AnnouncementResponse;
import Student_Management.class_service.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping("/teacher/{classId}/announcements")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @PathVariable Long classId,
            @Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(announcementService.createAnnouncement(classId, request));
    }

    @PutMapping("/teacher/announcements/{id}")
    public ResponseEntity<AnnouncementResponse> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.ok(announcementService.updateAnnouncement(id, request));
    }

    @DeleteMapping("/teacher/announcements/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teacher/{classId}/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncementsForTeacher(@PathVariable Long classId) {
        return ResponseEntity.ok(announcementService.getAnnouncements(classId));
    }

    @GetMapping("/student/{classId}/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncementsForStudent(@PathVariable Long classId) {
        return ResponseEntity.ok(announcementService.getAnnouncements(classId));
    }
}