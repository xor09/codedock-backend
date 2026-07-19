// package com.example.codedockbackend.controller;

// import com.example.codedockbackend.dto.SubmissionResponse;
// import com.example.codedockbackend.service.SubmissionService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.*;

// import java.util.UUID;

// @RestController
// @RequestMapping("/submissions")
// @RequiredArgsConstructor
// public class SubmissionController {

//     private final SubmissionService submissionService;

//     @GetMapping
//     public ResponseEntity<Page<SubmissionResponse>> listSubmissions(
//             Authentication authentication,
//             @RequestParam(defaultValue = "0") int page,
//             @RequestParam(defaultValue = "20") int size
//     ) {
//         return ResponseEntity.ok(
//                 submissionService.listForUser(authentication.getName(), page, size)
//         );
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<SubmissionResponse> getSubmission(
//             Authentication authentication,
//             @PathVariable UUID id
//     ) {
//         return ResponseEntity.ok(
//                 submissionService.getForUser(id, authentication.getName())
//         );
//     }
// }
