package Student_Management.class_service.service;

import Student_Management.class_service.dto.AnnouncementRequest;
import Student_Management.class_service.dto.AnnouncementResponse;
import Student_Management.class_service.entity.Announcement;
import Student_Management.class_service.exception.ResourceNotFoundException;
import Student_Management.class_service.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ClassService classService;

    @Transactional
    public AnnouncementResponse createAnnouncement(Long classId, AnnouncementRequest request, Long teacherId) {
        classService.validateTeacherOwnership(classId, teacherId);

        Announcement announcement = Announcement.builder()
                .classId(classId)
                .title(request.getTitle())
                .content(request.getContent())
                .teacherId(teacherId)
                .build();

        announcement = announcementRepository.save(announcement);
        return mapToResponse(announcement);
    }

    public List<AnnouncementResponse> getAnnouncementsByClassId(Long classId) {
        List<Announcement> announcements = announcementRepository.findByClassIdOrderByCreatedAtDesc(classId);
        return announcements.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AnnouncementResponse getAnnouncementById(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", announcementId));
        return mapToResponse(announcement);
    }

    @Transactional
    public AnnouncementResponse updateAnnouncement(Long announcementId, AnnouncementRequest request, Long teacherId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", announcementId));

        classService.validateTeacherOwnership(announcement.getClassId(), teacherId);

        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement = announcementRepository.save(announcement);
        return mapToResponse(announcement);
    }

    @Transactional
    public void deleteAnnouncement(Long announcementId, Long teacherId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", announcementId));

        classService.validateTeacherOwnership(announcement.getClassId(), teacherId);

        announcementRepository.delete(announcement);
    }

    private AnnouncementResponse mapToResponse(Announcement announcement) {
        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .classId(announcement.getClassId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .teacherId(announcement.getTeacherId())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }
}