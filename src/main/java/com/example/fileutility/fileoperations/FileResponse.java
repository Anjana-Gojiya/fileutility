package com.example.fileutility.fileoperations;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileResponse {

    private String fileName;
    private boolean success;
}
