package Student_Management.class_service.repository;

import Student_Management.class_service.entity.ClassMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassMemberRepository extends JpaRepository<ClassMember, Long> {

    List<ClassMember> findByClassId(Long classId);

    List<ClassMember> findByUserId(Long userId);

    Optional<ClassMember> findByClassIdAndUserId(Long classId, Long userId);

    List<ClassMember> findByClassIdAndRole(Long classId, String role);

    boolean existsByClassIdAndUserIdAndRole(Long classId, Long userId, String role);

    void deleteByClassIdAndUserId(Long classId, Long userId);
}