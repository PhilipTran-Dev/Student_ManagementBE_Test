package Student_Management.class_service.service;

import Student_Management.class_service.dto.ClassRequest;
import Student_Management.class_service.dto.ClassResponse;
import Student_Management.class_service.dto.UserDto;
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
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final ClassMemberRepository classMemberRepository;
    private final ClassCodeGenerator classCodeGenerator;
    private final WebClient userServiceWebClient;

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
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Long teacherId = currentUser.getId();

        //use webclient to call user-service to get teacher info in port 8081
        UserDto teacherDto = userServiceWebClient.get()
                .uri("/api/v1/teacher/" + teacherId) // call endpoint get teacher info by id
                .retrieve()
                .bodyToMono(UserDto.class)
                .block(); // run blocking

        String teacherName = (teacherDto != null) ? teacherDto.getFullName() : "Unknown Teacher";
        String teacherEmail = (teacherDto != null) ? teacherDto.getEmail() : "N/A";

        // take list class by teacherId from classRepository
        return classRepository.findByTeacherId(teacherId)
                .stream()
                .map(classroom -> {
                    ClassResponse response = convertToResponse(classroom);
                    response.setTeacherName(teacherName);
                    response.setTeacherEmail(teacherEmail);
                    return response;
                })
                .toList();
    }
}