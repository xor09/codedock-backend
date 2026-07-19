package com.example.codedockbackend.dto;

import com.example.codedockbackend.model.Submission;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SubmissionResponse {
    private static final int MAX_RESPONSE_TEXT_CHARS = 20_000;

    private UUID id;
    private String studentEmail;
    private String language;
    private String code;
    private String stdin;
    private Integer timeoutSeconds;
    private String filename;
    private String status;
    private String jobId;
    private Integer exitCode;
    private String stdout;
    private String stderr;
    private String error;
    private Instant submittedAt;
    private Instant startedAt;
    private Instant finishedAt;

    public static SubmissionResponse from(Submission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .studentEmail(submission.getUser().getEmail())
                .language(submission.getLanguage())
                .code(truncate(submission.getCode()))
                .stdin(truncate(submission.getStdin()))
                .timeoutSeconds(submission.getTimeoutSeconds())
                .filename(submission.getFilename())
                .status(submission.getStatus().name())
                .jobId(submission.getJobId())
                .exitCode(submission.getExitCode())
                .stdout(truncate(submission.getStdout()))
                .stderr(truncate(submission.getStderr()))
                .error(truncate(submission.getError()))
                .submittedAt(submission.getSubmittedAt())
                .startedAt(submission.getStartedAt())
                .finishedAt(submission.getFinishedAt())
                .build();
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= MAX_RESPONSE_TEXT_CHARS) {
            return value;
        }

        return value.substring(0, MAX_RESPONSE_TEXT_CHARS)
                + "\n[truncated after " + MAX_RESPONSE_TEXT_CHARS + " characters]";
    }
}
