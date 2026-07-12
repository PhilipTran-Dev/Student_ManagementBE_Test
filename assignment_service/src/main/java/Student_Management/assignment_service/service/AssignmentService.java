package Student_Management.assignment_service.service;

import Student_Management.assignment_service.dto.*;
import Student_Management.assignment_service.entity.*;
import Student_Management.assignment_service.repository.AssignmentRepository;
import Student_Management.assignment_service.repository.SubmissionRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final MinioClient minioClient;
    private final WebClient classServiceWebClient;

    @Value("${app.minio.bucket}")
    private String bucket;

    // ----- TEACHER LOGIC -----

    @Transactional
    public Assignment createAssignment(AssignmentRequest request, List<MultipartFile> files) {
        Long teacherId = getUserIdFromContext();
        verifyTeacherAccess(request.getClassId(), teacherId);

        List<String> fileUrls = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            fileUrls = uploadFilesToMinio(files);
        }

        Assignment assignment = Assignment.builder()
                .classId(request.getClassId())
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .maxMark(request.getMaxMark())
                .createdBy(teacherId)
                .attachments(fileUrls)
                .build();

        return assignmentRepository.save(assignment);
    }

    @Transactional
    public Assignment updateAssignment(Long id, AssignmentRequest request, List<MultipartFile> files) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found with id: " + id));

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDeadline(request.getDeadline());
        assignment.setMaxMark(request.getMaxMark());

        if (files != null && !files.isEmpty()) {
            List<String> currentFiles = assignment.getAttachments();
            if (currentFiles == null) {
                currentFiles = new ArrayList<>();
            }

            List<String> newUploadedFiles = uploadFilesToMinio(files);
            currentFiles.addAll(newUploadedFiles);

            assignment.setAttachments(currentFiles);
        }

        return assignmentRepository.save(assignment);
    }

    @Transactional
    public Submission gradeSubmission(Long submissionId, GradeRequest request) {
        Long teacherId = getUserIdFromContext();
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        verifyTeacherAccess(submission.getAssignment().getClassId(), teacherId);

        if (request.getGrade() > submission.getAssignment().getMaxMark() || request.getGrade() < 0) {
            throw new IllegalArgumentException("Invalid grade value");
        }

        submission.setGrade(request.getGrade());
        submission.setFeedback(request.getFeedback());

        return submissionRepository.save(submission);
    }

    // ----- STUDENT LOGIC -----

    @Transactional(readOnly = true)
    public List<StudentAssignmentResponse> getStudentAssignments(Long classId) {
        Long studentId = getUserIdFromContext();
        verifyStudentAccess(classId, studentId);

        List<Assignment> assignments = assignmentRepository.findByClassIdOrderByDeadlineDesc(classId);

        return assignments.stream().map(assignment -> {
            Submission submission = submissionRepository.findByAssignmentAndStudentId(assignment, studentId).orElse(null);
            return buildStudentResponse(assignment, submission);
        }).collect(Collectors.toList());
    }

    @Transactional
    public Submission submitAssignment(Long assignmentId, List<MultipartFile> files) {
        Long studentId = getUserIdFromContext();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        verifyStudentAccess(assignment.getClassId(), studentId);

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Must upload at least one file");
        }

        Submission existing = submissionRepository.findByAssignmentAndStudentId(assignment, studentId).orElse(null);
        if (existing != null) {
            throw new IllegalStateException("Already submitted. Please unsubmit first.");
        }

        List<String> fileUrls = uploadFilesToMinio(files);
        LocalDateTime now = LocalDateTime.now();
        SubmissionStatus status = now.isAfter(assignment.getDeadline()) ? SubmissionStatus.LATE : SubmissionStatus.ON_TIME;

        Submission submission = Submission.builder()
                .assignment(assignment)
                .studentId(studentId)
                .fileUrls(fileUrls)
                .status(status)
                .build();

        return submissionRepository.save(submission);
    }

    // ----- CORE PRIVATE METHODS -----

    private StudentAssignmentResponse buildStudentResponse(Assignment assignment, Submission submission) {
        AssignmentState state;
        if (submission != null) {
            state = AssignmentState.DONE;
        } else {
            state = LocalDateTime.now().isAfter(assignment.getDeadline().plusDays(2))
                    ? AssignmentState.MISSING : AssignmentState.TODO;
        }

        return StudentAssignmentResponse.builder()
                .id(assignment.getId())
                .classId(assignment.getClassId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .deadline(assignment.getDeadline())
                .maxMark(assignment.getMaxMark())
                .attachments(assignment.getAttachments())
                .state(state)
                .earnedGrade(submission != null ? submission.getGrade() : null)
                .feedback(submission != null ? submission.getFeedback() : null)
                .submittedAt(submission != null ? submission.getSubmittedAt() : null)
                .submissionStatus(submission != null ? submission.getStatus().name() : null)
                .build();
    }

    private List<String> uploadFilesToMinio(List<MultipartFile> files) {
        List<String> fileNames = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                String fileId = UUID.randomUUID() + "_" + file.getOriginalFilename();
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucket).object(fileId)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType()).build());
                fileNames.add(fileId);
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO Upload Failed", e);
        }
        return fileNames;
    }

    private Long getUserIdFromContext() {
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    private void verifyTeacherAccess(Long classId, Long teacherId) {
        // Giả lập hoặc gọi sang class-service qua WebClient
    }

    private void verifyStudentAccess(Long classId, Long studentId) {
        // Giả lập hoặc gọi sang class-service qua WebClient
    }
}