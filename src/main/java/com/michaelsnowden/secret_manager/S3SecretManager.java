package com.michaelsnowden.secret_manager;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by michael.snowden on 7/17/16.
 */
public class S3SecretManager extends SecretManager {
    private final AmazonS3Client s3Client;
    private String bucket;
    private String folder;

    public static S3SecretManager secretManager() {
        Configuration configuration = Configuration.configuration();
        byte[] cryptoKey = configuration.getCryptoKey();
        String s3SecretsBucket = configuration.getS3SecretsBucket();
        String s3SecretsFolder = configuration
                .getS3SecretsFolder();
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);
        return new S3SecretManager(cryptoKey, s3SecretsBucket, s3SecretsFolder, s3Client);
    }

    public S3SecretManager(byte[] cryptoKey, String bucket, String folder, AmazonS3Client s3Client) {
        super(cryptoKey);
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.folder = folder;
    }

    @Override
    public void deleteSecret(String id) {
        s3Client.deleteObject(new DeleteObjectRequest(bucket, folder + "/" + id));
    }

    @Override
    public List<String> listSecrets() {
        return s3Client.listObjects(bucket, folder)
                .getObjectSummaries()
                .stream()
                .skip(1)
                .map(s3ObjectSummary -> s3ObjectSummary.getKey()
                        .substring(folder.length() + 1))
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public byte[] readSecretBytes(String id) {
        try {
            S3Object object = s3Client.getObject(new GetObjectRequest(bucket, folder + "/" + id));
            return IOUtils.toByteArray(object.getObjectContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeSecretBytes(String id, byte[] bytes) {
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        PutObjectRequest request = new PutObjectRequest(bucket, folder + "/" + id, input, metadata);
        s3Client.putObject(request);
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
