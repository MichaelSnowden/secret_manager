package com.michaelsnowden.secret_manager;

import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Created by michael.snowden on 7/18/16.
 */
public class Secrets {
    static SecretManager localSecretManager(String key, File folder) {
        return new LocalSecretManager(key.getBytes(StandardCharsets.UTF_8), folder);
    }

    static SecretManager s3SecretManager(String key, AmazonS3Client s3Client, String folder, String bucket) {
        return new S3SecretManager(key.getBytes(StandardCharsets.UTF_8), bucket, folder, s3Client);
    }
}