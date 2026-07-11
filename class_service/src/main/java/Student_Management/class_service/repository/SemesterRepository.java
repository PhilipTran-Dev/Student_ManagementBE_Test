package Student_Management.class_service.repository;
import Student_Management.class_service.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
    Optional<Semester> findByCode(String code);
    boolean existsByCode(String code);
}
