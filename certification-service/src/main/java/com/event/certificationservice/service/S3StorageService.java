package com.event.certificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
public class S3StorageService {

    @Value("${aws.credentials.access-key}")
    private String accessKey;

    @Value("${aws.credentials.secret-key}")
    private String secretKey;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    private S3Client getS3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    /**
     * Uploads a PDF file from a byte array to S3.
     * @param data The byte array of the PDF file.
     * @param fileName The desired name of the file in the S3 bucket.
     * @return The public URL of the uploaded file.
     */
    public String uploadPdf(byte[] data, String fileName) {
        S3Client s3Client = getS3Client();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType("application/pdf")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));

        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
        log.info("PDF uploaded successfully to S3. URL: {}", fileUrl);
        return fileUrl;
    }

    /**
     * Downloads a file from S3 using its full URL.
     * @param fileUrl The public URL of the file in S3.
     * @return The byte array of the downloaded file.
     */
    public byte[] downloadFile(String fileUrl) {
        S3Client s3Client = getS3Client();

        // Extract the file key (the part of the URL after the bucket name)
        String fileKey = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
        log.info("File downloaded successfully from S3: {}", fileKey);
        return objectBytes.asByteArray();
    }
}