package com.example.fileutility.folderupload;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/upload")
public class FolderUploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FolderUploadController.class);

    @Autowired
    private Environment environment;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload (HttpServletRequest request) throws IOException, ServletException {
        Path PUBLIC_DIR = Paths.get(environment.getProperty("file.dir.path"));
        for (var part : request.getParts()) {

            // The name of the file is the relative path of the file in the client folder
            // Convert the client path separator to the server file path separator.
            String fileName = FilenameUtils.separatorsToSystem(part.getSubmittedFileName());

            // Resolve the absolute path to the file based on the public folder
            Path file = PUBLIC_DIR.resolve(fileName);

            // Try to create the folder where the file is located
            if (Files.notExists(file.getParent())) {
                Files.createDirectories(file.getParent());
            }

            // Write data to file
            try (var inputStream = part.getInputStream()){
                Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
            }

            LOGGER.info("write file: [{}] {}", part.getSize(), file);
        }
        return "ok";
    }
}
