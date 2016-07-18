package com.michaelsnowden.secret_manager;

/**
 * Created by michael.snowden on 7/18/16.
 */
public class SecretManagerException extends Exception {
    public SecretManagerException(String message) {
        super(message);
    }

    public SecretManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecretManagerException(Throwable cause) {
        super(cause);
    }
}
