package com.example.codedockbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class RunJobStatusResponse {
    private String jobId;
    private UUID submissionId;
    private String language;
    private String status;
    private Integer queuePosition;
    private RunResponse result;
    private String error;
    private Instant submittedAt;
    private Instant startedAt;
    private Instant finishedAt;
}
