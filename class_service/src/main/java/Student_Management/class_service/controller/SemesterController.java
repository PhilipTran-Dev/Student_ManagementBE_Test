package Student_Management.class_service.controller;

import Student_Management.class_service.dto.SemesterRequest;
import Student_Management.class_service.dto.SemesterResponse;
import Student_Management.class_service.service.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    // API public/authenticated dành cho Teacher/Student load dropdown list
    @GetMapping("/semesters")
    public ResponseEntity<List<SemesterResponse>> getAllSemesters() {
        return ResponseEntity.ok(semesterService.getAllSemesters());
    }

    // Các API độc quyền bảo mật cho ADMIN quản trị danh mục
    @PostMapping("/admin/semesters")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SemesterResponse> createSemester(@Valid @RequestBody SemesterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(semesterService.createSemester(request));
    }

    @PutMapping("/admin/semesters/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SemesterResponse> updateSemester(@PathVariable Long id, @Valid @RequestBody SemesterRequest request) {
        return ResponseEntity.ok(semesterService.updateSemester(id, request));
    }

    @DeleteMapping("/admin/semesters/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSemester(@PathVariable Long id) {
        semesterService.deleteSemester(id);
        return ResponseEntity.noContent().build();
    }
}
