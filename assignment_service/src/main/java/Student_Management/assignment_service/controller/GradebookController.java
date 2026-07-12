package Student_Management.assignment_service.controller;

import Student_Management.assignment_service.dto.ClassGradebookRow;
import Student_Management.assignment_service.service.GradebookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments/gradebook")
@RequiredArgsConstructor
public class GradebookController {

    private final GradebookService gradebookService;

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ClassGradebookRow>> getClassGradebook(@PathVariable Long classId) {
        return ResponseEntity.ok(gradebookService.getClassGradebook(classId));
    }

    @GetMapping("/class/{classId}/export")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<byte[]> exportGradebookToExcel(@PathVariable Long classId) {
        List<ClassGradebookRow> data = gradebookService.getClassGradebook(classId);
        byte[] mockExcelBytes = "StudentCode,StudentName,Average\n".getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=gradebook_class_" + classId + ".csv")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(mockExcelBytes);
    }
}