package Student_Management.class_service.service;

import Student_Management.class_service.dto.CourseRequest;
import Student_Management.class_service.dto.CourseResponse;
import Student_Management.class_service.entity.Course;
import Student_Management.class_service.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        if (courseRepository.existsByCode(request.getCode().trim().toUpperCase())) {
            throw new IllegalArgumentException("Course code already exists!");
        }

        Course course = Course.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .credits(request.getCredits())
                .level(request.getLevel())
                .facultyName(request.getFacultyName())
                .build();

        return mapToResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + id));

        if (!course.getCode().equals(request.getCode().trim().toUpperCase())
                && courseRepository.existsByCode(request.getCode().trim().toUpperCase())) {
            throw new IllegalArgumentException("New course code already exists!");
        }

        course.setCode(request.getCode().trim().toUpperCase());
        course.setName(request.getName().trim());
        course.setCredits(request.getCredits());
        course.setLevel(request.getLevel());
        course.setFacultyName(request.getFacultyName());

        return mapToResponse(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .credits(course.getCredits())
                .level(course.getLevel())
                .facultyName(course.getFacultyName())
                .build();
    }
}