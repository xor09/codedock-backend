package com.example.codedockbackend.service;

import com.example.codedockbackend.dto.RunRequest;
import com.example.codedockbackend.dto.RunResponse;
import com.example.codedockbackend.dto.SubmissionResponse;
import com.example.codedockbackend.model.Submission;
import com.example.codedockbackend.model.SubmissionStatus;
import com.example.codedockbackend.model.User;
import com.example.codedockbackend.repository.SubmissionRepository;
import com.example.codedockbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_STORED_TEXT_CHARS = 20_000;

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Submission create(String userEmail, String language, RunRequest request, SubmissionStatus status, String jobId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Submission submission = Submission.builder()
                .user(user)
                .language(language)
                .code(truncate(request.code))
                .stdin(truncate(request.stdin))
                .timeoutSeconds(request.timeoutSeconds)
                .filename(request.filename)
                .status(status)
                .jobId(jobId)
                .build();

        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission markRunning(UUID submissionId) {
        Submission submission = getSubmission(submissionId);
        submission.setStatus(SubmissionStatus.RUNNING);
        submission.setStartedAt(Instant.now());
        return submission;
    }

    @Transactional
    public Submission markCompleted(UUID submissionId, RunResponse result) {
        Submission submission = getSubmission(submissionId);
        submission.setStatus(classifyStatus(result));
        submission.setExitCode(result.getExitCode());
        submission.setStdout(truncate(result.getStdout()));
        submission.setStderr(truncate(result.getStderr()));
        submission.setError(null);
        submission.setFinishedAt(Instant.now());
        return submission;
    }

    @Transactional
    public Submission markFailed(UUID submissionId, String error) {
        Submission submission = getSubmission(submissionId);
        submission.setStatus(SubmissionStatus.FAILED);
        submission.setError(truncate(error));
        submission.setFinishedAt(Instant.now());
        return submission;
    }

    @Transactional(readOnly = true)
    public Page<SubmissionResponse> listForUser(String userEmail, int page, int size) {
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(
                sanitizedPage,
                sanitizedSize,
                Sort.by(Sort.Direction.DESC, "submittedAt")
        );

        return submissionRepository.findByUserEmail(userEmail, pageable)
                .map(SubmissionResponse::from);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getForUser(UUID id, String userEmail) {
        Submission submission = submissionRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        return SubmissionResponse.from(submission);
    }

    private Submission getSubmission(UUID submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
    }

    private SubmissionStatus classifyStatus(RunResponse result) {
        int exitCode = result.getExitCode();
        String combinedOutput = ((result.getStdout() == null ? "" : result.getStdout()) + "\n"
                + (result.getStderr() == null ? "" : result.getStderr())).toLowerCase();

        if (exitCode == 0) {
            return SubmissionStatus.COMPLETED;
        }

        if (exitCode == -1 || exitCode == 124 || combinedOutput.contains("time limit")
                || combinedOutput.contains("timeout")) {
            return SubmissionStatus.TLE;
        }

        if (exitCode == 137 || exitCode == 143 || combinedOutput.contains("memory limit")
                || combinedOutput.contains("outofmemory") || combinedOutput.contains("out of memory")
                || combinedOutput.contains("cannot allocate memory") || combinedOutput.contains("killed")) {
            return SubmissionStatus.MLE;
        }

        if (combinedOutput.contains("compilation") || combinedOutput.contains("compile")
                || combinedOutput.contains("javac") || combinedOutput.contains("g++")
                || combinedOutput.contains("syntaxerror")) {
            return SubmissionStatus.COMPILATION_ERROR;
        }

        return SubmissionStatus.RUNTIME_ERROR;
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_STORED_TEXT_CHARS) {
            return value;
        }

        return value.substring(0, MAX_STORED_TEXT_CHARS)
                + "\n[truncated after " + MAX_STORED_TEXT_CHARS + " characters]";
    }
}
