package Student_Management.class_service.controller;

import Student_Management.class_service.dto.*;
import Student_Management.class_service.service.AnnouncementService;
import Student_Management.class_service.service.ClassFileService;
import Student_Management.class_service.service.ClassMemberService;
import Student_Management.class_service.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
public class SharedClassController {

    private final ClassService classService;
    private final ClassMemberService classMemberService;
    private final AnnouncementService announcementService;
    private final ClassFileService classFileService;

    @GetMapping("/{classId}")
    public ResponseEntity<ClassResponse> getClassDetails(@PathVariable Long classId) {
        ClassResponse response = classService.getClassById(classId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{classId}/members")
    public ResponseEntity<List<ClassMemberResponse>> getClassMembers(@PathVariable Long classId) {
        List<ClassMemberResponse> members = classMemberService.getClassMembers(classId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{classId}/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements(@PathVariable Long classId) {
        List<AnnouncementResponse> announcements = announcementService.getAnnouncementsByClassId(classId);
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/{classId}/files")
    public ResponseEntity<List<ClassFileResponse>> getClassFiles(@PathVariable Long classId) {
        List<ClassFileResponse> files = classFileService.getFilesByClassId(classId);
        return ResponseEntity.ok(files);
    }
}