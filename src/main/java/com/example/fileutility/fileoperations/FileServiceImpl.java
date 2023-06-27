package com.example.fileutility.fileoperations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger LOGGER= LoggerFactory.getLogger(FileService.class);
    @Value("${file.dir.path}")
    private String fileUploadDir;
    private static final Set<String> validExtensions = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "pdf", "xlsx", "xls", "doc", "docx"));
    private static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L;
    private int failedUploadCount;

    @Override
    public ResponseEntity<FileResponse> uploadMultipleFile(List<MultipartFile> files, String userId) {
        failedUploadCount = 0;
        Path path = Paths.get(fileUploadDir + "/" + userId);
        if(!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity(
                files.stream().map(file -> uploadFile(file,userId)).collect(Collectors.toList())
                ,headers
                ,failedUploadCount > 0 ? HttpStatus.BAD_REQUEST : HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StreamingResponseBody> downloadFile(String fileName, String userId) {
        File file = new File(fileUploadDir + "/" + userId + "/" + fileName);

        // Set the appropriate headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        // Create the StreamingResponseBody to stream the file content
        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[8000];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        };

        // Return the ResponseEntity with headers and the StreamingResponseBody
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    @Override
    public ResponseEntity deleteFile(String fileName, String userId) {
        Path path = Paths.get(fileUploadDir + "/" + userId + "/" + fileName);
        try {
            if(Files.exists(path)) {
                Files.delete(path);
                return new ResponseEntity("File deleted successfully",HttpStatus.OK);
            } else {
                return new ResponseEntity("File does not exist",HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private FileResponse uploadFile(MultipartFile file,String userId){
        String extension = getFileExtension(file.getOriginalFilename());
        if(!isValidFile(file.getOriginalFilename(),file.getSize()) || extension == null){
            failedUploadCount++;
            return FileResponse.builder()
                    .fileName(file.getOriginalFilename())
                    .success(false)
                    .build();
        }

        String uniqueFileName = file.getOriginalFilename().replace("."+extension,"")
                + "_" + System.currentTimeMillis() + "." + extension;
        Path path = Paths.get(fileUploadDir + "/" + userId + "/" + uniqueFileName);
        try {
            Files.copy(file.getInputStream(),path);
            return FileResponse.builder()
                    .fileName(uniqueFileName)
                    .success(true)
                    .build();
        } catch (Exception e) {
            failedUploadCount++;
            LOGGER.error("Failed to Upload the file : " + file.getOriginalFilename());
            return FileResponse.builder()
                    .fileName(file.getOriginalFilename())
                    .success(false)
                    .build();
        }
    }

    private boolean isValidFile(String fileName,long fileSize) {
        // Check if the filename contains any invalid path sequence
        if (fileName.contains("..")) {
            LOGGER.error("Filename contains invalid path sequence " + fileName);
            return false;
        }
        // Check file size
        if (fileSize > MAX_FILE_SIZE) {
            LOGGER.error("Filename contains invalid path sequence " + fileName);
            return false;
        }
        return true;
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            String index = filename.substring(dotIndex + 1).toLowerCase();
            if(validExtensions.contains(index)) {
                return index;
            }
        }
        return null;
    }
}
