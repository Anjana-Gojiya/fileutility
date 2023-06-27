package com.example.fileutility.resumepause;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@RestController
@RequestMapping("/rpc")
public class ResumePauseController {

    private final RestTemplate restTemplate;

    @Value("${file.dir.path}")
    private String fileUploadDir;

    @Autowired
    public ResumePauseController(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    //normal simple download from a server
    @GetMapping("/download")
    public void downloadFile() throws IOException {
        String url = "https://www.africau.edu/images/default/sample.pdf";
        // Optional Accept header
        RequestCallback requestCallback = request -> request
                .getHeaders()
                .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));

        Path path = Paths.get(fileUploadDir + "/sample.pdf");
        // Streams the response instead of loading it all in memory
        ResponseExtractor<Void> responseExtractor = response -> {
            Files.copy(response.getBody(), path);
            return null;
        };
        restTemplate.execute(url, HttpMethod.GET, requestCallback, responseExtractor);
    }

    @GetMapping("/download/get-file-size")
    public ResponseEntity getFileSizeForDownload() throws IOException {
        return ResponseEntity.ok()
            .body("File size is " + getFileSize());
    }

    //using rest template
    @GetMapping("/download/get-file-chunk")
    public ResponseEntity getFileChunk(@RequestHeader(value = "Range",required = false) String rangeHeader) throws IOException {
        String url = "https://www.africau.edu/images/default/sample.pdf";

        long fileLength = getFileSize();
        long startByte = 0,endByte = fileLength - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            startByte = Integer.parseInt(ranges[0]);
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                endByte = Integer.parseInt(ranges[1]);
            }
            endByte = Math.min(endByte, fileLength - 1);
        }

        if(endByte > fileLength) {
            return ResponseEntity.badRequest()
                    .body("Range is out of file's length");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Range", "bytes=" + startByte + "-" + endByte);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Resource> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Resource.class);
        Resource resource = responseEntity.getBody();

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = resource.getInputStream()) {
                byte[] buffer = new byte[8000];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        };

        HttpHeaders headers1 = new HttpHeaders();
        headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=demo.pdf");
        return ResponseEntity.ok()
                .headers(headers1)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    private long getFileSize(){
        String url = "https://www.africau.edu/images/default/sample.pdf";
        RestTemplate restTemplate = new RestTemplate();
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        HttpHeaders headers = restTemplate.headForHeaders(uri);
        return headers.getContentLength();
    }
}
