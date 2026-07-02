package Student_Management.class_service.controller;

import Student_Management.class_service.dto.ClassFileResponse;
import Student_Management.class_service.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @PostMapping("/teacher/{classId}/materials/upload")
    public ResponseEntity<ClassFileResponse> uploadMaterial(
            @PathVariable Long classId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(materialService.uploadMaterial(classId, file));
    }

    @GetMapping("/teacher/{classId}/materials")
    public ResponseEntity<List<ClassFileResponse>> getMaterialsForTeacher(@PathVariable Long classId) {
        return ResponseEntity.ok(materialService.getMaterials(classId));
    }

    @GetMapping("/student/{classId}/materials")
    public ResponseEntity<List<ClassFileResponse>> getMaterialsForStudent(@PathVariable Long classId) {
        return ResponseEntity.ok(materialService.getMaterials(classId));
    }

    @GetMapping("/materials/{fileId}/download")
    public ResponseEntity<Map<String, String>> downloadMaterial(@PathVariable Long fileId) {
        String downloadUrl = materialService.generateDownloadUrl(fileId);
        return ResponseEntity.ok(Map.of("downloadUrl", downloadUrl));
    }

    @DeleteMapping("/teacher/materials/{fileId}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long fileId) {
        materialService.deleteMaterial(fileId);
        return ResponseEntity.noContent().build();
    }
}