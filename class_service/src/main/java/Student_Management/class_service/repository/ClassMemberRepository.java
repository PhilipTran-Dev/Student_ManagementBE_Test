package Student_Management.class_service.repository;

import Student_Management.class_service.entity.Class;
import Student_Management.class_service.entity.ClassMember;
import Student_Management.class_service.entity.ClassMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassMemberRepository extends JpaRepository<ClassMember, Long> {

    //find history to know if they have ever joined this class, regardless of ACTIVE or LEFT
    Optional<ClassMember> findByClassroomAndUserId(Class classroom, Long userId);
    //check user still active in class
    boolean existsByClassroomAndUserIdAndStatus(Class classroom, Long userId, ClassMemberStatus status);
    // take list of members who are currently ACTIVE in the class
    List<ClassMember> findByClassroomAndStatus(Class classroom, ClassMemberStatus status);
    // take list of classes that student is currently ACTIVE in
    List<ClassMember> findByUserIdAndRoleAndStatus(Long userId, String role, ClassMemberStatus status);
}