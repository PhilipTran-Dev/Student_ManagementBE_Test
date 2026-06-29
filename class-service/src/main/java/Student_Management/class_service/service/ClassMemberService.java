package Student_Management.class_service.service;

import Student_Management.class_service.dto.ClassMemberResponse;
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
public class ClassMemberService {

    private final ClassMemberRepository classMemberRepository;
    private final ClassRepository classRepository;
    private final ClassService classService;

    @Transactional
    public ClassMemberResponse joinClassByCode(String code, Long userId) {
        Class classEntity = classService.getClassEntityByCode(code);

        if (!"ACTIVE".equals(classEntity.getStatus())) {
            throw new BadRequestException("Class is not active");
        }

        // Check if already a member
        if (classMemberRepository.existsByClassIdAndUserIdAndRole(
                classEntity.getId(), userId, "STUDENT")) {
            throw new BadRequestException("Already enrolled in this class");
        }

        // Check if already enrolled as TEACHER
        if (classMemberRepository.existsByClassIdAndUserIdAndRole(
                classEntity.getId(), userId, "TEACHER")) {
            throw new BadRequestException("You are the teacher of this class, cannot join as student");
        }

        ClassMember member = ClassMember.builder()
                .classId(classEntity.getId())
                .userId(userId)
                .role("STUDENT")
                .build();

        member = classMemberRepository.save(member);

        // PLACEHOLDER LOGGING: Mock event instead of Kafka
        log.info("MOCK EVENT [StudentEnrolled]: Student ID {} successfully joined Class ID {}. Ready to notify Teacher ID {}",
                userId, classEntity.getId(), classEntity.getTeacherId());

        return mapToResponse(member);
    }

    @Transactional
    public void leaveClass(Long classId, Long userId) {
        ClassMember member = classMemberRepository.findByClassIdAndUserId(classId, userId)
                .orElseThrow(() -> new BadRequestException("You are not a member of this class"));

        if ("TEACHER".equals(member.getRole())) {
            throw new BadRequestException("Teacher cannot leave the class. Transfer ownership first.");
        }

        classMemberRepository.delete(member);
        log.info("User {} left class {}", userId, classId);
    }

    @Transactional
    public void removeStudent(Long classId, Long studentId, Long teacherId) {
        classService.validateTeacherOwnership(classId, teacherId);

        ClassMember member = classMemberRepository.findByClassIdAndUserId(classId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found in this class"));

        if (!"STUDENT".equals(member.getRole())) {
            throw new BadRequestException("User is not a student in this class");
        }

        classMemberRepository.delete(member);
        log.info("Teacher {} removed student {} from class {}", teacherId, studentId, classId);
    }

    public List<ClassMemberResponse> getClassMembers(Long classId) {
        // Verify class exists
        classService.getClassEntityById(classId);

        List<ClassMember> members = classMemberRepository.findByClassId(classId);
        return members.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ClassMemberResponse> getClassStudents(Long classId) {
        classService.getClassEntityById(classId);

        List<ClassMember> members = classMemberRepository.findByClassIdAndRole(classId, "STUDENT");
        return members.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public boolean isMemberOfClass(Long classId, Long userId) {
        return classMemberRepository.findByClassIdAndUserId(classId, userId).isPresent();
    }

    private ClassMemberResponse mapToResponse(ClassMember member) {
        return ClassMemberResponse.builder()
                .id(member.getId())
                .classId(member.getClassId())
                .userId(member.getUserId())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}