package com.example.fileutility.fileoperations;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

public interface FileService {

    ResponseEntity uploadMultipleFile(List<MultipartFile> files,String userId);
    ResponseEntity<StreamingResponseBody> downloadFile(String fileName, String userId);
    ResponseEntity deleteFile(String fileName,String userId);
}
