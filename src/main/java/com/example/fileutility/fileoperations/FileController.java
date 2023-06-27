package com.example.fileutility.fileoperations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping("/api/V1/file")
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);
    private FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity uploadFile(@RequestPart(value = "files") List<MultipartFile> files,
                                     @RequestParam("userId") String userId){
        if(!files.isEmpty()) {
            return fileService.uploadMultipleFile(files, userId);
        } else {
            return ResponseEntity.badRequest()
                    .body("No files provided for upload.");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity deleteFile(@RequestParam("fileName") String fileName,
                                     @RequestParam("userId") String userId){
        return fileService.deleteFile(fileName,userId);
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadFile(@RequestParam("fileName") String fileName,
                                                              @RequestParam("userId") String userId){
        return fileService.downloadFile(fileName,userId);
    }
}
