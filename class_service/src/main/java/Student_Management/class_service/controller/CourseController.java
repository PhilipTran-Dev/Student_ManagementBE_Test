package Student_Management.class_service.controller;

import Student_Management.class_service.dto.CourseRequest;
import Student_Management.class_service.dto.CourseResponse;
import Student_Management.class_service.service.CourseService;
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
public class CourseController {

    private final CourseService courseService;

    // API public/authenticated dành cho Teacher/Student load dropdown list
    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // Các API độc quyền bảo mật cho ADMIN quản trị danh mục
    @PostMapping("/admin/courses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
    }

    @PutMapping("/admin/courses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @DeleteMapping("/admin/courses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}