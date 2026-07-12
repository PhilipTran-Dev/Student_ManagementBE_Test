package Student_Management.assignment_service.controller;

import Student_Management.assignment_service.dto.TeacherSubmissionView;
import Student_Management.assignment_service.dto.UserPrincipal; // SỬA ĐỔI: Trỏ về DTO nội bộ
import Student_Management.assignment_service.entity.Submission;
import Student_Management.assignment_service.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping(value = "/student/{assignmentId}/submit", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Submission> submit(
            @PathVariable Long assignmentId,
            @RequestPart("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(submissionService.studentSubmit(assignmentId, principal.getId(), files));
    }

    @DeleteMapping("/student/{assignmentId}/unsubmit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> unsubmit(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        submissionService.studentUnsubmit(assignmentId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teacher/{assignmentId}/submissions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TeacherSubmissionView>> getSubmissions(
            @PathVariable Long assignmentId,
            @RequestParam(value = "status", required = false) String status) {
        return ResponseEntity.ok(submissionService.getTeacherSubmissionDashboard(assignmentId, status));
    }
}