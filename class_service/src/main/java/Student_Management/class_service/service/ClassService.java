package Student_Management.class_service.service;

import Student_Management.class_service.dto.ClassRequest;
import Student_Management.class_service.dto.ClassResponse;
import Student_Management.class_service.dto.UserPrincipal;
import Student_Management.class_service.entity.Class;
import Student_Management.class_service.entity.ClassMember;
import Student_Management.class_service.entity.ClassMemberStatus;
import Student_Management.class_service.repository.ClassMemberRepository;
import Student_Management.class_service.repository.ClassRepository;
import Student_Management.class_service.utils.ClassCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final ClassMemberRepository classMemberRepository;
    private final ClassCodeGenerator classCodeGenerator;

    @Transactional
    public ClassResponse createClass(ClassRequest request) {
        // take info teacher login from Spring Security Context
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Long teacherId = currentUser.getId();

        // auto-generate class code
        String classCode;
        do {
            classCode = classCodeGenerator.generateRandomCode();
        } while (classRepository.existsByCode(classCode));

        // Create a new Class entity and save the new class information
        Class classroom = Class.builder()
                .name(request.getName())
                .courseId(request.getCourseId())
                .semesterId(request.getSemesterId())
                .teacherId(teacherId)
                .code(classCode)
                .build();

        Class savedClass = classRepository.save(classroom);

        // insert teacher as a member of the class with ACTIVE status and TEACHER role
        ClassMember teacherMember = ClassMember.builder()
                .classroom(savedClass)
                .userId(teacherId)
                .role("TEACHER")
                .status(ClassMemberStatus.ACTIVE)
                .build();

        classMemberRepository.save(teacherMember);

        // return ClassResponse with class information and teacher's role
        return convertToResponse(savedClass);
    }

    private ClassResponse convertToResponse(Class classroom) {
        return ClassResponse.builder()
                .id(classroom.getId())
                .name(classroom.getName())
                .code(classroom.getCode())
                .courseId(classroom.getCourseId())
                .semesterId(classroom.getSemesterId())
                .teacherId(classroom.getTeacherId())
                .createdAt(classroom.getCreatedAt())
                .updatedAt(classroom.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ClassResponse> getTeacherClasses() {
        // automatically get the logged-in teacher's information from the JWT Token
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        // find classes in the DB by teacherId and map to ClassResponse
        return classRepository.findByTeacherId(currentUser.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }
}