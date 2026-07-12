package Student_Management.assignment_service.controller;

import Student_Management.assignment_service.dto.*;
import Student_Management.assignment_service.entity.Assignment;
import Student_Management.assignment_service.entity.Submission;
import Student_Management.assignment_service.repository.AssignmentRepository;
import Student_Management.assignment_service.repository.SubmissionRepository;
import Student_Management.assignment_service.service.AssignmentService;
import Student_Management.assignment_service.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentRepository assignmentRepository;

    // ----- TEACHER API -----

    @PostMapping(value = "/teacher/create", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Assignment> createAssignment(
            @RequestPart("data") @Valid AssignmentRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.createAssignment(request, files));
    }

    @PutMapping("/teacher/submissions/{submissionId}/grade")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Submission> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeRequest request) {
        return ResponseEntity.ok(assignmentService.gradeSubmission(submissionId, request));
    }

    @GetMapping("/teacher/class/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<Assignment>> getTeacherAssignments(@PathVariable Long classId) {
        return ResponseEntity.ok(assignmentRepository.findByClassIdOrderByDeadlineDesc(classId));
    }

    @PutMapping(value = "/teacher/{id}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Assignment> updateAssignment(
            @PathVariable Long id,
            @RequestPart("data") @Valid AssignmentRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, request, files));
    }

    // ----- STUDENT API -----

    @GetMapping("/student/class/{classId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<StudentAssignmentResponse>> getStudentAssignments(@PathVariable Long classId) {
        return ResponseEntity.ok(assignmentService.getStudentAssignments(classId));
    }

}