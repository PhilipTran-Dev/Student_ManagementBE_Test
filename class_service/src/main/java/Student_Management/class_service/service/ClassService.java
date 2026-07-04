package Student_Management.class_service.service;

import Student_Management.class_service.dto.*;
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
                .password(classroom.getPassword())
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

    //update password of class by classId, only teacher of this class can update password (this password is used for student to join class)
    @Transactional
    public ClassResponse updateClassPassword(Long classId, String password) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Class classroom = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find class with id: " + classId));

        //only teacher of this class can update password
        if (!classroom.getTeacherId().equals(currentUser.getId())) {
            throw new IllegalStateException("Only the teacher of this class can update the password!");
        }

        classroom.setPassword(password);
        Class updated = classRepository.save(classroom);
        return convertToResponse(updated);
    }

    // logic for student to join class by class code and password, if class code and password are correct, insert student into class_member table with ACTIVE status and STUDENT role
    @Transactional
    public void joinClass(JoinClassRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        String inputCode = request.getCode() != null ? request.getCode().trim() : "";
        // find class by class code
        Class classroom = classRepository.findByCode(inputCode)
                .orElseThrow(() -> new IllegalArgumentException("Class code is not exist!"));

        // checking security password if teacher set password for this class
        String classPassword = classroom.getPassword();
        if (classPassword != null && !classPassword.isBlank()) {
            if (request.getPassword() == null || !request.getPassword().equals(classPassword)) {
                throw new IllegalArgumentException("This password is not correct!");
            }
        }

        // check student has joined this class before, if yes, check status of this student in class_member table, if status is ACTIVE, throw exception, if status is INACTIVE, update status to ACTIVE
        java.util.Optional<ClassMember> existingMember = classMemberRepository
                .findByClassroomAndUserId(classroom, currentUser.getId());

        if (existingMember.isPresent()) {
            ClassMember member = existingMember.get();
            if (member.getStatus() == ClassMemberStatus.ACTIVE) {
                throw new IllegalStateException("You have joined this class before!");
            } else {
                // if status is INACTIVE, update status to ACTIVE
                member.setStatus(ClassMemberStatus.ACTIVE);
                classMemberRepository.save(member);
            }
        } else {
            // create new ClassMember entity with STUDENT role and ACTIVE status
            ClassMember newMember = ClassMember.builder()
                    .classroom(classroom)
                    .userId(currentUser.getId())
                    .role("STUDENT")
                    .status(ClassMemberStatus.ACTIVE)
                    .build();
            classMemberRepository.save(newMember);
        }
    }

    // logic to get list of classes that student has joined, only return classes with ACTIVE status in class_member table
    @Transactional(readOnly = true)
    public List<ClassResponse> getStudentClasses() {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        // find all ACTIVE class members with STUDENT role for current user
        List<ClassMember> memberships = classMemberRepository.findByUserIdAndRoleAndStatus(
                currentUser.getId(), "STUDENT", ClassMemberStatus.ACTIVE);

        return memberships.stream()
                .map(membership -> {
                    Class classroom = membership.getClassroom();
                    ClassResponse response = convertToResponse(classroom);

                    // call inter-service synchronously to user-service to get teacher info by teacherId
                    try {
                        UserDto teacherDto = userServiceWebClient.get()
                                .uri("/api/v1/teacher/" + classroom.getTeacherId())
                                .retrieve()
                                .bodyToMono(UserDto.class)
                                .block();
                        if (teacherDto != null) {
                            response.setTeacherName(teacherDto.getFullName());
                            response.setTeacherEmail(teacherDto.getEmail());
                        }
                    } catch (Exception e) {
                        response.setTeacherName("Annonymous Teacher");
                        response.setTeacherEmail("N/A");
                    }
                    return response;
                })
                .toList();
    }

    // get all list of students in a class by classId, only teacher of this class can get list of students, return list of StudentResponse with userId, fullName, email, status
    @Transactional(readOnly = true)
    public List<ClassMemberResponse> getClassMembers(Long classId) {
        Class classroom = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("cannot find class with id: " + classId));

        // get all list members with ACTIVE status in class_member table by classId
        List<ClassMember> activeMembers = classMemberRepository
                .findByClassroomAndStatus(classroom, ClassMemberStatus.ACTIVE);

        // filter out teacher, only get members with STUDENT role
        return activeMembers.stream()
                .filter(m -> "STUDENT".equalsIgnoreCase(m.getRole()))
                .map(member -> {
                    ClassMemberResponse.ClassMemberResponseBuilder builder = ClassMemberResponse.builder()
                            .userId(member.getUserId())
                            .joinedAt(member.getJoinedAt());

                    try {
                        // call endpoint of user-service to get detailed information (studentId, fullName, className)
                        StudentDetailDto studentDto = userServiceWebClient.get()
                                .uri("/api/v1/student/" + member.getUserId())
                                .retrieve()
                                .bodyToMono(StudentDetailDto.class)
                                .block();
                        if (studentDto != null) {
                            builder.fullName(studentDto.getFullName())
                                    .email(studentDto.getEmail())
                                    .studentId(studentDto.getStudentId())
                                    .className(studentDto.getClassName());
                        }
                    } catch (Exception e) {
                        builder.fullName("student name not found")
                                .email("N/A");
                    }
                    return builder.build();
                })
                .toList();
    }
}