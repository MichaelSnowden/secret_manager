package com.michaelsnowden.secret_manager;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Properties;

/**
 * Created by michael.snowden on 7/17/16.
 */
public interface SecretManager {
    List<String> listSecrets() throws SecretManagerException;

    Properties readSecret(String id) throws SecretManagerException;

    void writeSecret(String id, Properties secret) throws SecretManagerException;

    void deleteSecret(String id) throws SecretManagerException;

    default byte[] getRandomIv() throws CryptoException {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            byte[] iv = new byte[cipher.getBlockSize()];
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.nextBytes(iv);
            return iv;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new CryptoException(e);
        }
    }
}