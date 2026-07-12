package Student_Management.assignment_service.repository;

import Student_Management.assignment_service.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByClassIdOrderByDeadlineDesc(Long classId);
}