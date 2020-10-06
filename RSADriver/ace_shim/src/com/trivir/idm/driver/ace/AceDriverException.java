package com.trivir.idm.driver.ace;

class AceDriverException extends Exception {
    private static final long serialVersionUID = 1L;

    public AceDriverException(String message) {
        super(message);
    }

    public AceDriverException(String message, Throwable cause) {
        super(message, cause);
    }
}
