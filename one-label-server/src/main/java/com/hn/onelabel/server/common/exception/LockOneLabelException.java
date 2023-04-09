package com.hn.onelabel.server.common.exception;

public class LockOneLabelException extends RuntimeException {

    public LockOneLabelException() {
        super();
    }

    public LockOneLabelException(String message) {
        super(message);
    }

    public LockOneLabelException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockOneLabelException(Throwable cause) {
        super(cause);
    }

}
