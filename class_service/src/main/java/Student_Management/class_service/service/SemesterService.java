package Student_Management.class_service.service;

import Student_Management.class_service.dto.SemesterRequest;
import Student_Management.class_service.dto.SemesterResponse;
import Student_Management.class_service.entity.Semester;
import Student_Management.class_service.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;

    @Transactional(readOnly = true)
    public List<SemesterResponse> getAllSemesters() {
        return semesterRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public SemesterResponse createSemester(SemesterRequest request) {
        if (semesterRepository.existsByCode(request.getCode().trim().toUpperCase())) {
            throw new IllegalArgumentException("Semester code already exists!");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date!");
        }

        Semester semester = Semester.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus().trim().toUpperCase())
                .build();

        return mapToResponse(semesterRepository.save(semester));
    }

    @Transactional
    public SemesterResponse updateSemester(Long id, SemesterRequest request) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found with id: " + id));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date!");
        }

        semester.setCode(request.getCode().trim().toUpperCase());
        semester.setName(request.getName().trim());
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        semester.setStatus(request.getStatus().trim().toUpperCase());

        return mapToResponse(semesterRepository.save(semester));
    }

    @Transactional
    public void deleteSemester(Long id) {
        if (!semesterRepository.existsById(id)) {
            throw new IllegalArgumentException("Semester not found with id: " + id);
        }
        semesterRepository.deleteById(id);
    }

    private SemesterResponse mapToResponse(Semester semester) {
        return SemesterResponse.builder()
                .id(semester.getId())
                .code(semester.getCode())
                .name(semester.getName())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .status(semester.getStatus())
                .build();
    }
}