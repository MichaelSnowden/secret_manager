package com.michaelsnowden.secret_manager;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by michael.snowden on 7/18/16.
 */
public class LocalSecretManager extends SecretManager {
    private final File folder;

    public static LocalSecretManager localSecretManager() {
        Configuration configuration = Configuration.configuration();
        byte[] cryptoKey = configuration.getCryptoKey();
        String pathname = configuration.getLocalSecretsFolder();
        if (pathname == null) {
            String message = "'localSecretsFolder' must be set in the config file";
            throw new IllegalArgumentException(message);
        }
        File localSecretsFolder = new File(pathname);
        return new LocalSecretManager(cryptoKey, localSecretsFolder);
    }

    LocalSecretManager(byte[] cryptoKey, File folder) {
        super(cryptoKey);
        this.folder = folder;
    }

    @Override
    public List<String> listSecrets() {
        return Arrays.stream(folder.listFiles())
                .map(File::getName)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] readSecretBytes(String id) {
        try (FileInputStream fileInputStream = new FileInputStream(new File(folder, id))) {
            return IOUtils.toByteArray(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeSecretBytes(String id, byte[] secret) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(folder, id))) {
            fileOutputStream.write(secret);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteSecret(String id) {
        new File(folder, id).delete();
    }
}