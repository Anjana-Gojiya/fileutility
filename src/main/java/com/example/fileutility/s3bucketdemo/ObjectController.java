package com.example.fileutility.s3bucketdemo;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.FileUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/objects")
public class ObjectController {

    private AmazonS3 amazonS3Client;

    public ObjectController(AmazonS3 amazonS3) {
        this.amazonS3Client = amazonS3;
    }

    @GetMapping("/list-objects")
    public ResponseEntity listObjects(String bucketName){
        ObjectListing objectListing = amazonS3Client.listObjects(bucketName);
        return ResponseEntity.ok(objectListing.getObjectSummaries());
    }

    @GetMapping("download-objects")
    public void downloadObject(String bucketName, String objectName){
        //objectname is the whole key not just the foldername or the filename
        S3Object s3object = amazonS3Client.getObject(bucketName, objectName);
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        try {
            FileUtils.copyInputStreamToFile(inputStream, new File("." + File.separator + objectName));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @DeleteMapping("delete-object")
    public void deleteObject(String bucketName, String objectName){
        amazonS3Client.deleteObject(bucketName, objectName);
    }

    @DeleteMapping("delete-objects")
    public void deleteMultipleObjects(String bucketName, List<String> objects){
        DeleteObjectsRequest delObjectsRequests = new DeleteObjectsRequest(bucketName)
                .withKeys(objects.toArray(new String[0]));
        amazonS3Client.deleteObjects(delObjectsRequests);
    }

    @GetMapping("move-object")
    public void moveObject(String bucketSourceName, String objectName, String bucketTargetName){
        amazonS3Client.copyObject(
                bucketSourceName,
                objectName,
                bucketTargetName,
                objectName
        );
    }
}
