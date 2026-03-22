package com.example.codedockbackend.controller;

import com.example.codedockbackend.dto.RunRequest;
import com.example.codedockbackend.dto.RunResponse;
import com.example.codedockbackend.service.DockerRunnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/run")
@RequiredArgsConstructor
public class RunController {
    private final DockerRunnerService dockerRunnerService;

    @PostMapping("/{language}")
    public ResponseEntity<RunResponse> run(@PathVariable String language, @RequestBody RunRequest request) throws Exception{
        RunResponse response = this.dockerRunnerService.run(language, request);
        return ResponseEntity.ok(response);
    }
}
