package Student_Management.user_service.repository;

import Student_Management.user_service.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findByEmailAndCodeAndPurpose(String email, String code, String purpose);
}