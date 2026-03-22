package com.example.codedockbackend;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class CodedockBackendApplication {

    static {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(e ->
                System.setProperty(e.getKey(), e.getValue())
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(CodedockBackendApplication.class, args);
    }

    @Autowired
    Environment env;

    @PostConstruct
    public void testProps() {
        System.out.println("🟢 DB URL = " + env.getProperty("spring.datasource.url"));
    }


}
