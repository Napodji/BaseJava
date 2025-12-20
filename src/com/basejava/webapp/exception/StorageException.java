package com.basejava.webapp.exception;

/**
 * Base exception for storage errors
 */
public class StorageException extends RuntimeException {

    private final String uuid;

    public StorageException(String message) {
        this(message, null);
    }

    public StorageException(String message, String uuid) {
        super(message);
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
