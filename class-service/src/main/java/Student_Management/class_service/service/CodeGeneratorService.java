package Student_Management.class_service.service;

import Student_Management.class_service.repository.ClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class CodeGeneratorService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ClassRepository classRepository;

    public String generateUniqueCode() {
        String code;
        int maxAttempts = 100;
        int attempts = 0;

        do {
            code = generateRandomCode();
            attempts++;
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Unable to generate unique class code after " + maxAttempts + " attempts");
            }
        } while (classRepository.existsByCode(code));

        return code;
    }

    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
}