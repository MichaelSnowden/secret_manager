package com.michaelsnowden.secret_manager;

import org.apache.commons.lang3.ArrayUtils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Properties;

import static java.security.SecureRandom.getInstance;

/**
 * Created by michael.snowden on 7/17/16.
 */
public abstract class SecretManager {
    private final byte[] cryptoKey;

    public SecretManager(byte[] cryptoKey) {
        this.cryptoKey = cryptoKey;
    }

    public abstract List<String> listSecrets() throws SecretManagerException;

    public abstract void deleteSecret(String id) throws SecretManagerException;

    public final void writeSecret(String id, Properties secret) throws SecretManagerException {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            byte[] iv = new byte[cipher.getBlockSize()];
            SecureRandom secureRandom = getInstance("SHA1PRNG");
            secureRandom.nextBytes(iv);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            secret.store(output, null);
            byte[] unencryptedBytes = output.toByteArray();
            byte[] encryptedBytes = new Encryptor(cryptoKey, iv).encrypt(unencryptedBytes);
            byte[] allBytes = ArrayUtils.addAll(iv, encryptedBytes);
            writeSecretBytes(id, allBytes);
        } catch (CryptoException | IOException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new SecretManagerException(e);
        }
    }

    public final Properties readSecret(String id) throws SecretManagerException {
        try {
            byte[] fileBytes = readSecretBytes(id);
            byte[] iv = new byte[16];
            System.arraycopy(fileBytes, 0, iv, 0, 16);
            byte[] encryptedData = new byte[fileBytes.length - 16];
            System.arraycopy(fileBytes, 16, encryptedData, 0, fileBytes.length - 16);
            byte[] decryptedData = new Decryptor(cryptoKey, iv).decrypt(encryptedData);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decryptedData);
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (CryptoException | IOException e) {
            throw new SecretManagerException(e);
        }
    }

    protected abstract void writeSecretBytes(String id, byte[] secret) throws SecretManagerException;

    protected abstract byte[] readSecretBytes(String id) throws SecretManagerException;
}