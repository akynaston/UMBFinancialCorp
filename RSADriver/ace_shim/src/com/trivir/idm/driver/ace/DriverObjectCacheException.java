package com.trivir.idm.driver.ace;

public class DriverObjectCacheException extends Exception {
    public DriverObjectCacheException(String message) {
        super(message);
    }

    public DriverObjectCacheException(String message, Throwable t) {
    	super(message,t);
    }

    private static final long serialVersionUID = 1L;

}
