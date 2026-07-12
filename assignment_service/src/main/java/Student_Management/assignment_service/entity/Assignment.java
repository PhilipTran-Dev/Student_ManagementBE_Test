package Student_Management.assignment_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(name = "max_mark", nullable = false)
    private Double maxMark;

    @Column(name = "created_by", nullable = false)
    private Long createdBy; // teacher_id từ JWT

    @ElementCollection
    @CollectionTable(name = "assignment_attachments", joinColumns = @JoinColumn(name = "assignment_id"))
    @Column(name = "file_id")
    private List<String> attachments; // Lưu danh sách các file ID được upload lên MinIO

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}