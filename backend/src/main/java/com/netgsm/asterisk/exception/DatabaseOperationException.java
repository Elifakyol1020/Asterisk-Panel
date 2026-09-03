package com.netgsm.asterisk.exception;
public class DatabaseOperationException extends PlatformException {
    public DatabaseOperationException() { super(500, "DATABASE_ERROR", "Database operation failed"); }
}
