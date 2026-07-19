package com.example.codedockbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateUserResponse {
    private UUID id;
    private String name;
    private String email;
    private String role;
}
