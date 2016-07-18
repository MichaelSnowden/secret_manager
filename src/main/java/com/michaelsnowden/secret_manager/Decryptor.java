package com.michaelsnowden.secret_manager;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by michael.snowden on 7/17/16.
 */
public class Decryptor {
    final Cipher cipher;

    public Decryptor(byte[] key, byte[] iv) throws CryptoException {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new CryptoException(e);
        }
    }

    public byte[] decrypt(byte[] bytes) throws CryptoException {
        try {
            return cipher.doFinal(bytes);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new CryptoException(e);
        }
    }
}