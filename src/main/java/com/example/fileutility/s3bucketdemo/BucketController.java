package com.example.fileutility.s3bucketdemo;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bucket")
public class BucketController {

    private AmazonS3 amazonS3Client;

    public BucketController(AmazonS3 amazonS3) {
        this.amazonS3Client = amazonS3;
    }

    @GetMapping("/create-bucket")
    public ResponseEntity createS3Bucket(String bucketName) {
        if(amazonS3Client.doesBucketExist(bucketName)) {
            return ResponseEntity.badRequest().body("Bucket name already in use. Try another name.");
        }
        amazonS3Client.createBucket(bucketName);
        return ResponseEntity.ok("Bucket created successfully");
    }

    @GetMapping("/list-buckets")
    public ResponseEntity listBuckets(){
        return ResponseEntity.ok(amazonS3Client.listBuckets());
    }

    @GetMapping("/delete-bucket")
    public ResponseEntity deleteBucket(String bucketName){
        try {
            amazonS3Client.deleteBucket(bucketName);
            return ResponseEntity.ok(bucketName + " bucket deleted successfully");
        } catch (AmazonServiceException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
