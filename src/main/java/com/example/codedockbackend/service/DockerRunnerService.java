package com.example.codedockbackend.service;

import com.example.codedockbackend.dto.RunRequest;
import com.example.codedockbackend.dto.RunResponse;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

@Service
public class DockerRunnerService {

    private final Path runnersPath;
    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    public DockerRunnerService() throws Exception {
        String root = System.getProperty("user.dir");
        this.runnersPath = Paths.get(root, "runners", "work");
        Files.createDirectories(this.runnersPath);
        System.out.println("🔥 DockerRunnerService at " + this.runnersPath);
    }

    public RunResponse run(String lang, RunRequest req) throws Exception {

        String runId = "run-" + Instant.now().getEpochSecond();
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

        int timeout = req.timeoutSeconds != null ? req.timeoutSeconds : 5;

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

        Future<String> output = pool.submit(() -> {
            try (InputStream is = process.getInputStream()) {
                return new String(is.readAllBytes());
            }
        });

        boolean finished = process.waitFor(timeout + 2, TimeUnit.SECONDS);

        String out = output.get(1, TimeUnit.SECONDS);

        return new RunResponse(
                finished ? process.exitValue() : -1,
                out,
                finished ? "" : "Timeout"
        );
    }
}
