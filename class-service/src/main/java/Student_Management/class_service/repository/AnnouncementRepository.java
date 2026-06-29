package Student_Management.class_service.repository;

import Student_Management.class_service.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByClassIdOrderByCreatedAtDesc(Long classId);
}