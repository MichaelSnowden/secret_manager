package com.michaelsnowden.secret_manager;

import com.amazonaws.util.IOUtils;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by michael.snowden on 7/18/16.
 */
public class LocalSecretManager implements SecretManager {
    private final byte[] key;
    private final File folder;

    LocalSecretManager(byte[] key, File folder) {
        this.key = key;
        this.folder = folder;
    }

    @Override
    public List<String> listSecrets() throws SecretManagerException {
        return Arrays.stream(folder.listFiles())
                .map(File::getName)
                .collect(Collectors.toList());
    }

    @Override
    public Properties readSecret(String id) throws SecretManagerException {
        try (FileInputStream fileInputStream = new FileInputStream(new File(folder, id))) {
            byte[] fileBytes = IOUtils.toByteArray(fileInputStream);
            byte[] iv = new byte[16];
            System.arraycopy(fileBytes, 0, iv, 0, 16);
            byte[] encryptedData = new byte[fileBytes.length - 16];
            System.arraycopy(fileBytes, 16, encryptedData, 0, fileBytes.length - 16);
            byte[] decryptedData = new Decryptor(key, iv).decrypt(encryptedData);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decryptedData);
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException | CryptoException e) {
            throw new SecretManagerException(e);
        }
    }

    @Override
    public void writeSecret(String id, Properties secret) throws SecretManagerException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(folder, id))) {
            byte [] iv = getRandomIv();
            fileOutputStream.write(iv);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            secret.store(output, null);
            fileOutputStream.write(new Encryptor(key, iv).encrypt(output.toByteArray()));
        } catch (IOException | CryptoException e) {
            throw new SecretManagerException(e);
        }
    }

    @Override
    public void deleteSecret(String id) throws SecretManagerException {
        new File(folder, id).delete();
    }
}