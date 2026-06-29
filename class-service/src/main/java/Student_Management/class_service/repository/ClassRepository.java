package Student_Management.class_service.repository;

import Student_Management.class_service.entity.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {

    Optional<Class> findByCode(String code);

    List<Class> findByTeacherId(Long teacherId);

    boolean existsByCode(String code);
}