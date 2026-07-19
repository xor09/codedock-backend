package com.example.codedockbackend.service;

import com.example.codedockbackend.dto.RunJobStatusResponse;
import com.example.codedockbackend.dto.RunRequest;
import com.example.codedockbackend.dto.RunResponse;
import com.example.codedockbackend.model.Submission;
import com.example.codedockbackend.model.SubmissionStatus;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@RequiredArgsConstructor
public class RunQueueService {

    private final DockerRunnerService dockerRunnerService;
    private final SubmissionService submissionService;
    private final BlockingQueue<RunJob> queue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<String, RunJob> jobs = new ConcurrentHashMap<>();
    private Thread worker;
    private volatile boolean running = true;

    @PostConstruct
    public void startWorker() {
        worker = new Thread(this::processJobs, "code-runner-queue-worker");
        worker.setDaemon(true);
        worker.start();
    }

    public RunJobStatusResponse submit(String userEmail, String language, RunRequest request) {
        String jobId = UUID.randomUUID().toString();
        Submission submission = submissionService.create(
                userEmail,
                language,
                request,
                SubmissionStatus.QUEUED,
                jobId
        );
        RunJob job = new RunJob(jobId, submission.getId(), language, request);

        jobs.put(jobId, job);
        queue.offer(job);

        return toResponse(job);
    }

    public RunJobStatusResponse getStatus(String jobId) {
        RunJob job = jobs.get(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Run job not found");
        }

        return toResponse(job);
    }

    private void processJobs() {
        while (running) {
            try {
                RunJob job = queue.take();
                job.status = RunJobStatus.RUNNING;
                job.startedAt = Instant.now();
                submissionService.markRunning(job.submissionId);

                try {
                    job.result = dockerRunnerService.run(job.language, job.request);
                    job.status = classifyJobStatus(job.result);
                    submissionService.markCompleted(job.submissionId, job.result);
                } catch (Exception ex) {
                    job.error = ex.getMessage() != null ? ex.getMessage() : "Code execution failed";
                    job.status = RunJobStatus.FAILED;
                    submissionService.markFailed(job.submissionId, job.error);
                } finally {
                    job.finishedAt = Instant.now();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    private RunJobStatusResponse toResponse(RunJob job) {
        return RunJobStatusResponse.builder()
                .jobId(job.jobId)
                .submissionId(job.submissionId)
                .language(job.language)
                .status(job.status.name())
                .queuePosition(queuePosition(job.jobId))
                .result(job.result)
                .error(job.error)
                .submittedAt(job.submittedAt)
                .startedAt(job.startedAt)
                .finishedAt(job.finishedAt)
                .build();
    }

    private Integer queuePosition(String jobId) {
        ArrayList<RunJob> snapshot = new ArrayList<>(queue);
        for (int i = 0; i < snapshot.size(); i++) {
            if (snapshot.get(i).jobId.equals(jobId)) {
                return i + 1;
            }
        }
        return null;
    }

    @PreDestroy
    public void stopWorker() {
        running = false;
        if (worker != null) {
            worker.interrupt();
        }
    }

    private enum RunJobStatus {
        QUEUED,
        RUNNING,
        COMPLETED,
        FAILED,
        TLE,
        MLE,
        COMPILATION_ERROR,
        RUNTIME_ERROR
    }

    private RunJobStatus classifyJobStatus(RunResponse result) {
        int exitCode = result.getExitCode();
        String combinedOutput = ((result.getStdout() == null ? "" : result.getStdout()) + "\n"
                + (result.getStderr() == null ? "" : result.getStderr())).toLowerCase();

        if (exitCode == 0) {
            return RunJobStatus.COMPLETED;
        }

        if (exitCode == -1 || exitCode == 124 || combinedOutput.contains("time limit")
                || combinedOutput.contains("timeout")) {
            return RunJobStatus.TLE;
        }

        if (exitCode == 137 || exitCode == 143 || combinedOutput.contains("memory limit")
                || combinedOutput.contains("outofmemory") || combinedOutput.contains("out of memory")
                || combinedOutput.contains("cannot allocate memory") || combinedOutput.contains("killed")) {
            return RunJobStatus.MLE;
        }

        if (combinedOutput.contains("compilation") || combinedOutput.contains("compile")
                || combinedOutput.contains("javac") || combinedOutput.contains("g++")
                || combinedOutput.contains("syntaxerror")) {
            return RunJobStatus.COMPILATION_ERROR;
        }

        return RunJobStatus.RUNTIME_ERROR;
    }

    private static class RunJob {
        private final String jobId;
        private final UUID submissionId;
        private final String language;
        private final RunRequest request;
        private final Instant submittedAt = Instant.now();
        private volatile RunJobStatus status = RunJobStatus.QUEUED;
        private volatile RunResponse result;
        private volatile String error;
        private volatile Instant startedAt;
        private volatile Instant finishedAt;

        private RunJob(String jobId, UUID submissionId, String language, RunRequest request) {
            this.jobId = jobId;
            this.submissionId = submissionId;
            this.language = language;
            this.request = request;
        }
    }
}
