package com.michaelsnowden.secret_manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Configuration {
    private final byte[] cryptoKey;
    private final String localSecretsFolder;
    private final String s3SecretsBucket;
    private final String s3SecretsFolder;

    public static Configuration configuration() {
        File secretManagerDirectory = new File(System.getProperty("user.home"), ".secretManager");
        Properties config = new Properties();
        File configFile = new File(secretManagerDirectory, "config.properties");
        try {
            config.load(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
            String message = "Please make sure that the config file is located at " + configFile.getAbsolutePath() +
                    ".Here, you should set the 'encryptionKey' to a 16-character-long string. You should also set " +
                    "'localSecretsFolder' or 's3SecretsBucket' and 's3SecretsFolder' depending on whether you are " +
                    "using S3 or the local file system to store secrets";
            throw new RuntimeException(message, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String cryptoKey = config.getProperty("cryptoKey");
        if (cryptoKey == null) {
            String message = "'cryptoKey' must be set in the config file\nPlease edit the config file located at " +
                    configFile.getAbsolutePath();
            throw new IllegalArgumentException(message);
        }
        if (cryptoKey.length() != 16) {
            throw new CryptoKeyLengthException(cryptoKey);
        }
        byte[] bytes = cryptoKey.getBytes(StandardCharsets.UTF_8);
        String localSecretsFolder = config.getProperty("localSecretsFolder");
        String s3SecretsBucket = config.getProperty("s3SecretsBucket");
        String s3SecretsFolder = config.getProperty("s3SecretsFolder");
        return new Configuration(bytes, localSecretsFolder, s3SecretsBucket, s3SecretsFolder);
    }

    private Configuration(byte[] cryptoKey, String localSecretsFolder, String s3SecretsBucket, String s3SecretsFolder) {
        this.cryptoKey = cryptoKey;
        this.localSecretsFolder = localSecretsFolder;
        this.s3SecretsBucket = s3SecretsBucket;
        this.s3SecretsFolder = s3SecretsFolder;
    }

    public byte[] getCryptoKey() {
        return cryptoKey;
    }

    public String getLocalSecretsFolder() {
        return localSecretsFolder;
    }

    public String getS3SecretsBucket() {
        return s3SecretsBucket;
    }

    public String getS3SecretsFolder() {
        return s3SecretsFolder;
    }
}
