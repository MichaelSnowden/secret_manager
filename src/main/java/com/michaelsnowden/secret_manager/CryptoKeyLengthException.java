package com.michaelsnowden.secret_manager;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by michael.snowden on 7/18/16.
 */
public class CryptoKeyLengthException extends RuntimeException {
    public CryptoKeyLengthException(String cryptoKey) {
        super("The crypto key starting with " + StringUtils.left(cryptoKey, 4) +
                " must be 16 characters long. Instead it is " + cryptoKey.length() + " characters long.");
    }
}