package Student_Management.class_service.repository;

import Student_Management.class_service.entity.Class;
import Student_Management.class_service.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    //take list of announcements of a class, sort by createdAt desc
    List<Announcement> findByClassroomOrderByCreatedAtDesc(Class classroom);
}