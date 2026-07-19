package com.example.codedockbackend.service;

import com.example.codedockbackend.dto.RunRequest;
import com.example.codedockbackend.dto.RunResponse;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class DockerRunnerService {

    private static final int DEFAULT_TIMEOUT_SECONDS = 5;
    private static final int MAX_TIMEOUT_SECONDS = 10;
    private static final int MAX_OUTPUT_CHARS = 20_000;

    private final Path runnersPath;
    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    public DockerRunnerService() throws Exception {
        String root = System.getProperty("user.dir");
        this.runnersPath = Paths.get(root, "runners", "work");
        Files.createDirectories(this.runnersPath);
        System.out.println("🔥 DockerRunnerService at " + this.runnersPath);
    }

    public RunResponse run(String lang, RunRequest req) throws Exception {

        String runId = "run-" + UUID.randomUUID();
        Path jobDir = runnersPath.resolve(runId);
        Files.createDirectories(jobDir);

        String filename = switch (lang) {
            case "cpp" -> "Solution.cpp";
            case "java" -> "Main.java";
            case "python" -> "script.py";
            default -> throw new RuntimeException("Unsupported language");
        };

        Path srcFile = jobDir.resolve(filename);
        Files.writeString(srcFile, req.code);

        Path stdinFile = jobDir.resolve("stdin.txt");
        Files.writeString(stdinFile, req.stdin == null ? "" : req.stdin);

        int timeout = sanitizeTimeout(req.timeoutSeconds);

        List<String> command = List.of(
                "bash",
                Paths.get(System.getProperty("user.dir"), "runners", "run_code.sh").toString(),
                lang,
                srcFile.toAbsolutePath().toString(),
                stdinFile.toAbsolutePath().toString(),
                String.valueOf(timeout)
        );

        System.out.println("🔥 CMD = " + String.join(" ", command));

        return exec(command, timeout);
    }

    private RunResponse exec(List<String> cmd, long timeout) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        Future<CapturedOutput> output = pool.submit(() -> captureOutput(process.getInputStream()));

        boolean finished = process.waitFor(timeout + 2, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            process.waitFor(2, TimeUnit.SECONDS);
        }

        CapturedOutput capturedOutput;
        try {
            capturedOutput = output.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            output.cancel(true);
            capturedOutput = new CapturedOutput("", true);
        }

        String out = capturedOutput.value();
        if (capturedOutput.truncated()) {
            out = out + "\n[output truncated after " + MAX_OUTPUT_CHARS + " characters]";
        }

        boolean timedOut = !finished;
        int exitCode = timedOut ? -1 : process.exitValue();
        String stderr = classifyExecutionError(exitCode, timedOut);

        return new RunResponse(
                exitCode,
                out,
                stderr
        );
    }

    private String classifyExecutionError(int exitCode, boolean timedOut) {
        if (timedOut || exitCode == 124) {
            return "Time Limit Exceeded (TLE)";
        }

        if (exitCode == 137 || exitCode == 143) {
            return "Memory Limit Exceeded (MLE)";
        }

        return "";
    }

    private int sanitizeTimeout(Integer requestedTimeout) {
        int timeout = requestedTimeout != null ? requestedTimeout : DEFAULT_TIMEOUT_SECONDS;
        return Math.min(Math.max(timeout, 1), MAX_TIMEOUT_SECONDS);
    }

    private CapturedOutput captureOutput(InputStream inputStream) throws IOException {
        try (InputStream is = inputStream) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(MAX_OUTPUT_CHARS);
            byte[] chunk = new byte[4096];
            int total = 0;
            boolean truncated = false;
            int read;

            while ((read = is.read(chunk)) != -1) {
                int remaining = MAX_OUTPUT_CHARS - total;
                if (remaining > 0) {
                    int bytesToWrite = Math.min(read, remaining);
                    buffer.write(chunk, 0, bytesToWrite);
                    total += bytesToWrite;
                }

                if (read > remaining) {
                    truncated = true;
                }
            }

            return new CapturedOutput(buffer.toString(StandardCharsets.UTF_8), truncated);
        }
    }

    private record CapturedOutput(String value, boolean truncated) {
    }
}
