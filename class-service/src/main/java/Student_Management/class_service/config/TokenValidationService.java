package Student_Management.class_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@Slf4j
public class TokenValidationService {

    private final WebClient webClient;

    public TokenValidationService(
            WebClient.Builder webClientBuilder,
            @Value("${app.services.user-service-url}") String userServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(userServiceUrl).build();
    }

    public Map<String, Object> validateToken(String token) {
        try {
            log.debug("Validating token with user-service");

            return webClient.get()
                    .uri("/api/v1/auth/validate")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return null;
        }
    }
}