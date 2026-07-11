package Student_Management.class_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private Integer credits;

    @Column(length = 50)
    private String level; // e.g., Undergraduate, Graduate

    @Column(name = "faculty_name", length = 100)
    private String facultyName;
}