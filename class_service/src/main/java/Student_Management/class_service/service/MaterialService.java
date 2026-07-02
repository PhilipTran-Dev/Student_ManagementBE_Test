package Student_Management.class_service.service;

import Student_Management.class_service.dto.ClassFileResponse;
import Student_Management.class_service.dto.UserPrincipal;
import Student_Management.class_service.entity.Class;
import Student_Management.class_service.entity.ClassFile;
import Student_Management.class_service.repository.ClassFileRepository;
import Student_Management.class_service.repository.ClassRepository;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final ClassFileRepository classFileRepository;
    private final ClassRepository classRepository;
    private final MinioClient minioClient;

    @Value("${app.minio.bucket}")
    private String bucket;

    @Transactional
    public ClassFileResponse uploadMaterial(Long classId, MultipartFile file) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Class classroom = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("cannot find class with id: " + classId));

        if (!classroom.getTeacherId().equals(currentUser.getId())) {
            throw new IllegalStateException("only the teacher of this class can upload materials");
        }

        //create a random UUID for the file name to avoid name collisions
        String fileId = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            // push binary file to Object Storage MinIO system
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileId)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // save Metadata info to Postgres database
            ClassFile classFile = ClassFile.builder()
                    .classroom(classroom)
                    .fileId(fileId)
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .uploadedBy(currentUser.getId())
                    .build();

            ClassFile saved = classFileRepository.save(classFile);
            return convertToResponse(saved);

        } catch (Exception e) {
            throw new RuntimeException("error when upload file", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ClassFileResponse> getMaterials(Long classId) {
        Class classroom = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("cannot find class with id: " + classId));

        return classFileRepository.findByClassroomOrderByCreatedAtDesc(classroom)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public String generateDownloadUrl(Long fileId) {
        ClassFile classFile = classFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("data is not exist with id: " + fileId));

        try {
            //create link to download file from Object Storage MinIO system
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(classFile.getFileId())
                            .expiry(15, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("error when create link ", e);
        }
    }

    @Transactional
    public void deleteMaterial(Long fileId) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        ClassFile classFile = classFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("data is not exist with id: " + fileId));

        if (!classFile.getUploadedBy().equals(currentUser.getId())) {
            throw new IllegalStateException("cannot delete file that is not uploaded by you");
        }

        try {
            //delete file from Object Storage MinIO system
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(classFile.getFileId())
                            .build()
            );

            //delete metadata info from Postgres database
            classFileRepository.delete(classFile);

        } catch (Exception e) {
            throw new RuntimeException("error when delete file", e);
        }
    }

    private ClassFileResponse convertToResponse(ClassFile file) {
        return ClassFileResponse.builder()
                .id(file.getId())
                .classId(file.getClassroom().getId())
                .fileName(file.getFileName())
                .fileSize(file.getFileSize())
                .uploadedBy(file.getUploadedBy())
                .createdAt(file.getCreatedAt())
                .build();
    }
}