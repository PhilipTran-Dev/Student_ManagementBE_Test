package Student_Management.class_service.controller;

import Student_Management.class_service.config.SecurityUtil;
import Student_Management.class_service.dto.ClassMemberResponse;
import Student_Management.class_service.dto.ClassResponse;
import Student_Management.class_service.dto.JoinClassRequest;
import Student_Management.class_service.service.ClassMemberService;
import Student_Management.class_service.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student/classes")
@RequiredArgsConstructor
public class StudentClassController {

    private final ClassService classService;
    private final ClassMemberService classMemberService;

    @PostMapping("/join")
    public ResponseEntity<ClassMemberResponse> joinClass(@Valid @RequestBody JoinClassRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        ClassMemberResponse response = classMemberService.joinClassByCode(request.getCode(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{classId}/leave")
    public ResponseEntity<Void> leaveClass(@PathVariable Long classId) {
        Long userId = SecurityUtil.getCurrentUserId();
        classMemberService.leaveClass(classId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ClassResponse>> getMyClasses() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<ClassResponse> classes = classService.getClassesByUserId(userId);
        return ResponseEntity.ok(classes);
    }
}