package com.example.codedockbackend.controller;

import com.example.codedockbackend.dto.RunRequest;
import com.example.codedockbackend.dto.RunResponse;
import com.example.codedockbackend.dto.RunJobStatusResponse;
import com.example.codedockbackend.model.Submission;
import com.example.codedockbackend.model.SubmissionStatus;
import com.example.codedockbackend.service.DockerRunnerService;
import com.example.codedockbackend.service.RunQueueService;
import com.example.codedockbackend.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/run")
@RequiredArgsConstructor
public class RunController {
    private final RunQueueService runQueueService;

    @PostMapping("/{language}/queue")
    public ResponseEntity<RunJobStatusResponse> queueRun(
            Authentication authentication,
            @PathVariable String language,
            @RequestBody RunRequest request
    ) {
        return ResponseEntity.accepted().body(
                runQueueService.submit(authentication.getName(), language, request)
        );
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<RunJobStatusResponse> getRunJob(@PathVariable String jobId) {
        return ResponseEntity.ok(runQueueService.getStatus(jobId));
    }
}
