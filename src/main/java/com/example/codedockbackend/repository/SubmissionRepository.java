package com.example.codedockbackend.repository;

import com.example.codedockbackend.model.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    Page<Submission> findByUserEmail(String email, Pageable pageable);

    Optional<Submission> findByIdAndUserEmail(UUID id, String email);
}
