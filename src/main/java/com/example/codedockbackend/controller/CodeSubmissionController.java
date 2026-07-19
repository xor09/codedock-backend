package com.example.codedockbackend.controller;

import com.example.codedockbackend.dto.SubmissionHistoryResponse;
import com.example.codedockbackend.dto.SubmissionResponse;
import com.example.codedockbackend.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/code-submissions")
@RequiredArgsConstructor
public class CodeSubmissionController {

    private final SubmissionService submissionService;

    @GetMapping("/history") // get submission history of a particular user
    public ResponseEntity<SubmissionHistoryResponse> getHistory(
            Authentication authentication,
            @RequestParam(required = false) String studentEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String authenticatedEmail = authentication.getName();
        validateOwnEmail(studentEmail, authenticatedEmail);

        Page<SubmissionResponse> submissions = submissionService.listForUser(
                authenticatedEmail,
                page,
                size
        );

        return ResponseEntity.ok(
                SubmissionHistoryResponse.builder()
                        .submissions(submissions.getContent())
                        .page(submissions.getNumber())
                        .size(submissions.getSize())
                        .totalElements(submissions.getTotalElements())
                        .totalPages(submissions.getTotalPages())
                        .first(submissions.isFirst())
                        .last(submissions.isLast())
                        .build()
        );
    }

    @GetMapping("/{id}") //get submission by submission id of a particular user
    public ResponseEntity<SubmissionResponse> getSubmission(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestParam(required = false) String studentEmail
    ) {
        String authenticatedEmail = authentication.getName();
        validateOwnEmail(studentEmail, authenticatedEmail);

        return ResponseEntity.ok(
                submissionService.getForUser(id, authenticatedEmail)
        );
    }

    private void validateOwnEmail(String requestedEmail, String authenticatedEmail) {
        if (requestedEmail == null || requestedEmail.isBlank()) {
            return;
        }

        if (!requestedEmail.equalsIgnoreCase(authenticatedEmail)) {
            throw new AccessDeniedException("You can only access your own submissions");
        }
    }
}
