package com.example.codedockbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submissions", indexes = {
        @Index(name = "idx_submissions_user_submitted_at", columnList = "user_id, submitted_at"),
        @Index(name = "idx_submissions_job_id", columnList = "job_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String language;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String stdin;

    private Integer timeoutSeconds;

    private String filename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @Column(name = "job_id", unique = true)
    private String jobId;

    private Integer exitCode;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String stdout;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String stderr;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String error;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    private Instant startedAt;

    private Instant finishedAt;
}
