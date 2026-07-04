package Student_Management.class_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "classes", indexes = {
        @Index(name = "idx_class_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Class {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // compatible with SERIAL/BIGSERIAL Postgres
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String code; // Random code from 6-10 characters, unique for each class

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId; // has been cited from jwt user when creating class

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 100, nullable = false)
    private String password; // password when student wanto to join class, optional, can be null
}