package Student_Management.class_service.service;

import Student_Management.class_service.dto.ClassFileResponse;
import Student_Management.class_service.entity.ClassFile;
import Student_Management.class_service.exception.ResourceNotFoundException;
import Student_Management.class_service.repository.ClassFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassFileService {

    private final ClassFileRepository classFileRepository;
    private final ClassService classService;

    public List<ClassFileResponse> getFilesByClassId(Long classId) {
        classService.getClassEntityById(classId);

        List<ClassFile> files = classFileRepository.findByClassId(classId);
        return files.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ClassFileResponse mapToResponse(ClassFile classFile) {
        return ClassFileResponse.builder()
                .id(classFile.getId())
                .classId(classFile.getClassId())
                .fileId(classFile.getFileId())
                .fileName(classFile.getFileName())
                .fileUrl(classFile.getFileUrl())
                .uploadedBy(classFile.getUploadedBy())
                .createdAt(classFile.getCreatedAt())
                .build();
    }
}