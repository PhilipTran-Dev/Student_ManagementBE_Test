package Student_Management.class_service.controller;

import Student_Management.class_service.config.SecurityUtil;
import Student_Management.class_service.dto.*;
import Student_Management.class_service.service.AnnouncementService;
import Student_Management.class_service.service.ClassMemberService;
import Student_Management.class_service.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teacher/classes")
@RequiredArgsConstructor
public class TeacherClassController {

    private final ClassService classService;
    private final ClassMemberService classMemberService;
    private final AnnouncementService announcementService;

    @PostMapping
    public ResponseEntity<ClassResponse> createClass(@Valid @RequestBody ClassRequest request) {
        Long teacherId = SecurityUtil.getCurrentUserId();
        ClassResponse response = classService.createClass(request, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ClassResponse>> getMyClasses() {
        Long teacherId = SecurityUtil.getCurrentUserId();
        List<ClassResponse> classes = classService.getClassesByTeacherId(teacherId);
        return ResponseEntity.ok(classes);
    }

    // --- Announcement CRUD ---

    @PostMapping("/{classId}/announcements")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @PathVariable Long classId,
            @Valid @RequestBody AnnouncementRequest request) {
        Long teacherId = SecurityUtil.getCurrentUserId();
        AnnouncementResponse response = announcementService.createAnnouncement(classId, request, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{classId}/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements(@PathVariable Long classId) {
        List<AnnouncementResponse> announcements = announcementService.getAnnouncementsByClassId(classId);
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/{classId}/announcements/{announcementId}")
    public ResponseEntity<AnnouncementResponse> getAnnouncement(
            @PathVariable Long classId,
            @PathVariable Long announcementId) {
        AnnouncementResponse announcement = announcementService.getAnnouncementById(announcementId);
        return ResponseEntity.ok(announcement);
    }

    @PutMapping("/{classId}/announcements/{announcementId}")
    public ResponseEntity<AnnouncementResponse> updateAnnouncement(
            @PathVariable Long classId,
            @PathVariable Long announcementId,
            @Valid @RequestBody AnnouncementRequest request) {
        Long teacherId = SecurityUtil.getCurrentUserId();
        AnnouncementResponse response = announcementService.updateAnnouncement(announcementId, request, teacherId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{classId}/announcements/{announcementId}")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long classId,
            @PathVariable Long announcementId) {
        Long teacherId = SecurityUtil.getCurrentUserId();
        announcementService.deleteAnnouncement(announcementId, teacherId);
        return ResponseEntity.noContent().build();
    }

    // --- Student Management ---

    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<Void> removeStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        Long teacherId = SecurityUtil.getCurrentUserId();
        classMemberService.removeStudent(classId, studentId, teacherId);
        return ResponseEntity.noContent().build();
    }
}