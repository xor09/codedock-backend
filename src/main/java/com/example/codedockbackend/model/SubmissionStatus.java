package com.example.codedockbackend.model;

public enum SubmissionStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    TLE,
    MLE,
    COMPILATION_ERROR,
    RUNTIME_ERROR
}
