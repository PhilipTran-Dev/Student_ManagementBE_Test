package Student_Management.class_service.controller;

import Student_Management.class_service.dto.ClassRequest;
import Student_Management.class_service.dto.ClassResponse;
import Student_Management.class_service.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    // create api for teacher to create class
    @PostMapping("/teacher/create")
    public ResponseEntity<ClassResponse> createClass(@Valid @RequestBody ClassRequest request) {
        ClassResponse response = classService.createClass(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // get all classes of teacher jwt
    @GetMapping("/teacher/all")
    public ResponseEntity<List<ClassResponse>> getTeacherClasses() {
        List<ClassResponse> responses = classService.getTeacherClasses();
        return ResponseEntity.ok(responses);
    }
}