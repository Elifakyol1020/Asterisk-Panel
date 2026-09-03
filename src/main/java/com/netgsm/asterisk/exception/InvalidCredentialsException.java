package com.netgsm.asterisk.exception;
public class InvalidCredentialsException extends PlatformException {
    public InvalidCredentialsException() { super(401, "UNAUTHORIZED", "Invalid credentials"); }
}
