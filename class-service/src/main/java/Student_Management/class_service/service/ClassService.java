package Student_Management.class_service.service;

import Student_Management.class_service.dto.ClassRequest;
import Student_Management.class_service.dto.ClassResponse;
import Student_Management.class_service.entity.Class;
import Student_Management.class_service.entity.ClassMember;
import Student_Management.class_service.exception.BadRequestException;
import Student_Management.class_service.exception.ResourceNotFoundException;
import Student_Management.class_service.repository.ClassMemberRepository;
import Student_Management.class_service.repository.ClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassService {

    private final ClassRepository classRepository;
    private final ClassMemberRepository classMemberRepository;
    private final CodeGeneratorService codeGeneratorService;

    @Transactional
    public ClassResponse createClass(ClassRequest request, Long teacherId) {
        String code = codeGeneratorService.generateUniqueCode();

        Class classEntity = Class.builder()
                .name(request.getName())
                .code(code)
                .courseId(request.getCourseId())
                .semesterId(request.getSemesterId())
                .teacherId(teacherId)
                .status("ACTIVE")
                .build();

        classEntity = classRepository.save(classEntity);

        // Auto-enroll the teacher as a member with role TEACHER
        ClassMember teacherMember = ClassMember.builder()
                .classId(classEntity.getId())
                .userId(teacherId)
                .role("TEACHER")
                .build();
        classMemberRepository.save(teacherMember);

        log.info("Class created with code: {} by teacher: {}", code, teacherId);

        return mapToResponse(classEntity);
    }

    public List<ClassResponse> getClassesByTeacherId(Long teacherId) {
        List<Class> classes = classRepository.findByTeacherId(teacherId);
        return classes.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ClassResponse getClassById(Long classId) {
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", classId));
        return mapToResponse(classEntity);
    }

    public Class getClassEntityById(Long classId) {
        return classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", classId));
    }

    public ClassResponse getClassByCode(String code) {
        Class classEntity = classRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "code", code));
        return mapToResponse(classEntity);
    }

    public Class getClassEntityByCode(String code) {
        return classRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "code", code));
    }

    public List<ClassResponse> getClassesByUserId(Long userId) {
        List<ClassMember> memberships = classMemberRepository.findByUserId(userId);
        List<Long> classIds = memberships.stream()
                .map(ClassMember::getClassId)
                .toList();
        return classRepository.findAllById(classIds).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void validateTeacherOwnership(Long classId, Long teacherId) {
        Class classEntity = getClassEntityById(classId);
        if (!classEntity.getTeacherId().equals(teacherId)) {
            throw new BadRequestException("Teacher does not own this class");
        }
    }

    private ClassResponse mapToResponse(Class classEntity) {
        return ClassResponse.builder()
                .id(classEntity.getId())
                .name(classEntity.getName())
                .code(classEntity.getCode())
                .courseId(classEntity.getCourseId())
                .semesterId(classEntity.getSemesterId())
                .teacherId(classEntity.getTeacherId())
                .status(classEntity.getStatus())
                .createdAt(classEntity.getCreatedAt())
                .updatedAt(classEntity.getUpdatedAt())
                .build();
    }
}