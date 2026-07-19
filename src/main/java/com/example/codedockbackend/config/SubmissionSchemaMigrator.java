package com.example.codedockbackend.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionSchemaMigrator {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void updateSubmissionStatusConstraint() {
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                    IF to_regclass('public.submissions') IS NOT NULL THEN
                        ALTER TABLE submissions
                            DROP CONSTRAINT IF EXISTS submissions_status_check;

                        ALTER TABLE submissions
                            ADD CONSTRAINT submissions_status_check
                            CHECK (status IN (
                                'QUEUED',
                                'RUNNING',
                                'COMPLETED',
                                'FAILED',
                                'TLE',
                                'MLE',
                                'COMPILATION_ERROR',
                                'RUNTIME_ERROR'
                            ));
                    END IF;
                END $$;
                """);
    }
}
