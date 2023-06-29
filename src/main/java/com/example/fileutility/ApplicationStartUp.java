package com.example.fileutility;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ApplicationStartUp {

    private static final Logger LOGGER= LoggerFactory.getLogger(ApplicationStartUp.class);

    @Value("${file.dir.path}")
    private String fileDirPath;

    @PostConstruct
    public void init() {
        makeDirectories();
    }

    private void makeDirectories() {
        try {
            Path path = Paths.get(fileDirPath);
            if(!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to create directory: " + e.getMessage());
        }
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("")
                .build();
    }
}
