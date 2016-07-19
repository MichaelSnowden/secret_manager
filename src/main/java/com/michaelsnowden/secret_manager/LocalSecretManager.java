package com.michaelsnowden.secret_manager;

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

    LocalSecretManager(byte[] cryptoKey, File folder) {
        super(cryptoKey);
        this.folder = folder;
    }

    @Override
    public List<String> listSecrets() throws SecretManagerException {
        return Arrays.stream(folder.listFiles())
                .map(File::getName)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] readSecretBytes(String id) throws SecretManagerException {
        try (FileInputStream fileInputStream = new FileInputStream(new File(folder, id))) {
            return IOUtils.toByteArray(fileInputStream);
        } catch (IOException e) {
            throw new SecretManagerException(e);
        }
    }

    @Override
    public void writeSecretBytes(String id, byte [] secret) throws SecretManagerException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(folder, id))) {
            fileOutputStream.write(secret);
        } catch (IOException e) {
            throw new SecretManagerException(e);
        }
    }

    @Override
    public void deleteSecret(String id) throws SecretManagerException {
        new File(folder, id).delete();
    }
}