package Student_Management.assignment_service.service;

import Student_Management.assignment_service.dto.ClassGradebookRow;
import Student_Management.assignment_service.dto.ClassMemberResponse;
import Student_Management.assignment_service.entity.Assignment;
import Student_Management.assignment_service.entity.Submission;
import Student_Management.assignment_service.repository.AssignmentRepository;
import Student_Management.assignment_service.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GradebookService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final WebClient classServiceWebClient;

    public List<ClassGradebookRow> getClassGradebook(Long classId) {
        // take all mark columns (Assignments) of this class
        List<Assignment> assignments = assignmentRepository.findByClassIdOrderByDeadlineDesc(classId);

        // take all students of this class
        List<ClassMemberResponse> members = classServiceWebClient.get()
                .uri("/api/v1/classes/{classId}/members", classId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ClassMemberResponse>>() {})
                .block();

        if (members == null) return Collections.emptyList();

        List<ClassGradebookRow> gradebookRows = new ArrayList<>();

        // calculate the gradebook matrix
        for (ClassMemberResponse member : members) {
            Map<Long, Double> studentGrades = new HashMap<>();
            double totalScore = 0.0;
            int gradedCount = 0;

            for (Assignment asm : assignments) {
                Optional<Submission> sub = submissionRepository.findByAssignmentAndStudentId(asm, member.getUserId());
                if (sub.isPresent() && sub.get().getGrade() != null) {
                    double normalizedGrade = sub.get().getGrade();
                    studentGrades.put(asm.getId(), normalizedGrade);
                    totalScore += normalizedGrade;
                    gradedCount++;
                } else {
                    studentGrades.put(asm.getId(), 0.0); // if no submission or ungraded, consider as 0
                }
            }

            double average = gradedCount > 0 ? (totalScore / (assignments.size() * 10.0)) * 10.0 : 0.0;

            gradebookRows.add(ClassGradebookRow.builder()
                    .studentId(member.getUserId())
                    .studentName(member.getFullName())
                    .studentCode(member.getStudentId())
                    .assignmentGrades(studentGrades)
                    .totalAverage(average)
                    .build());
        }

        return gradebookRows;
    }
}