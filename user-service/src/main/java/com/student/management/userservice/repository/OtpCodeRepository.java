package com.student.management.userservice.repository;

import com.student.management.userservice.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findByEmailAndCodeAndPurpose(String email, String code, String purpose);
}