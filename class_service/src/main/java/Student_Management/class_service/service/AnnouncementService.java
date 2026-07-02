package Student_Management.class_service.service;

import Student_Management.class_service.dto.AnnouncementRequest;
import Student_Management.class_service.dto.AnnouncementResponse;
import Student_Management.class_service.dto.UserDto;
import Student_Management.class_service.dto.UserPrincipal;
import Student_Management.class_service.entity.Announcement;
import Student_Management.class_service.entity.Class;
import Student_Management.class_service.repository.AnnouncementRepository;
import Student_Management.class_service.repository.ClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ClassRepository classRepository;
    private final WebClient userServiceWebClient;

    @Transactional
    public AnnouncementResponse createAnnouncement(Long classId, AnnouncementRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Class classroom = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("cannot find class with id: " + classId));

        // make sure only teacher manage this class can create announcement
        if (!classroom.getTeacherId().equals(currentUser.getId())) {
            throw new IllegalStateException("you are not the teacher of this class, cannot create announcement");
        }

        Announcement announcement = Announcement.builder()
                .classroom(classroom)
                .title(request.getTitle())
                .content(request.getContent())
                .authorId(currentUser.getId())
                .build();

        Announcement saved = announcementRepository.save(announcement);
        return convertToResponse(saved, null);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getAnnouncements(Long classId) {
        Class classroom = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("cannot find class with id: " + classId));

        // call synchronously via WebClient to user-service to get teacher name
        UserDto teacherDto = userServiceWebClient.get()
                .uri("/api/v1/teacher/" + classroom.getTeacherId())
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();

        String authorName = (teacherDto != null) ? teacherDto.getFullName() : "Teacher";

        return announcementRepository.findByClassroomOrderByCreatedAtDesc(classroom)
                .stream()
                .map(announcement -> convertToResponse(announcement, authorName))
                .toList();
    }

    @Transactional
    public AnnouncementResponse updateAnnouncement(Long id, AnnouncementRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("cannot find announcement with id: " + id));

        if (!announcement.getAuthorId().equals(currentUser.getId())) {
            throw new IllegalStateException("you cannot update this announcement because you are not the author");
        }

        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        Announcement updated = announcementRepository.save(announcement);

        return convertToResponse(updated, null);
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("cannot find announcement with id: " + id));

        if (!announcement.getAuthorId().equals(currentUser.getId())) {
            throw new IllegalStateException("you cannot delete this announcement because you are not the author");
        }

        announcementRepository.delete(announcement);
    }

    private AnnouncementResponse convertToResponse(Announcement announcement, String authorName) {
        return AnnouncementResponse.builder()
                .id(announcement.getId())
                .classId(announcement.getClassroom().getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .authorId(announcement.getAuthorId())
                .authorName(authorName)
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }
}