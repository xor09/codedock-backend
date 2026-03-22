package com.example.codedockbackend.dto;

public class RunRequest {
    public String code;
    public String stdin;
    public Integer timeoutSeconds = 5;
    public String filename;

    @Override
    public String toString() {
        return "RunRequest{" +
                "code='" + code + '\'' +
                ", stdin='" + stdin + '\'' +
                ", timeoutSeconds=" + timeoutSeconds +
                ", filename='" + filename + '\'' +
                '}';
    }
}
