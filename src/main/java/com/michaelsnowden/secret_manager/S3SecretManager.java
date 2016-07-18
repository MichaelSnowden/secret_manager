package com.michaelsnowden.secret_manager;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.google.common.collect.ImmutableMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by michael.snowden on 7/17/16.
 */
public class S3SecretManager implements SecretManager {
    private final AmazonS3Client s3Client;
    private final String bucket;
    private final String folder;
    private final byte[] encryptionKey;

    public S3SecretManager(byte[] encryptionKey, String bucket, String folder, AmazonS3Client s3Client) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.folder = folder;
        this.encryptionKey = encryptionKey;
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
    public Properties readSecret(String id) throws SecretManagerException {
        try {
            S3Object object = s3Client.getObject(new GetObjectRequest(bucket, folder + "/" + id));
            ObjectMetadata objectMetadata = object.getObjectMetadata();
            String encodedIv = objectMetadata.getUserMetaDataOf("iv");
            byte[] iv = Base64.getDecoder()
                    .decode(encodedIv);
            S3ObjectInputStream objectContent = object.getObjectContent();
            Decryptor decryptor = new Decryptor(encryptionKey, iv);
            byte[] bytes = decryptor.decrypt(IOUtils.toByteArray(objectContent));
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Properties properties = new Properties();
            properties.load(byteArrayInputStream);
            return properties;
        } catch (CryptoException | IOException e) {
            throw new SecretManagerException(e);
        }
    }

    @Override
    public void writeSecret(String id, Properties secret) throws SecretManagerException {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            secret.store(output, null);
            byte[] iv = getRandomIv();
            Encryptor encryptor = new Encryptor(encryptionKey, iv);
            byte[] bytes = encryptor.encrypt(output.toByteArray());
            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setUserMetadata(ImmutableMap.of("iv", Base64.getEncoder()
                    .encodeToString(iv)));
            metadata.setContentLength(bytes.length);
            PutObjectRequest request = new PutObjectRequest(bucket, folder + "/" + id, input, metadata);
            s3Client.putObject(request);
        } catch (CryptoException | IOException e) {
            throw new SecretManagerException(e);
        }
    }
}
