package com.netgsm.asterisk.exception;
public class DuplicateResourceException extends PlatformException {
    public DuplicateResourceException(String resource) { super(409, "CONFLICT", resource + " already exists"); }
}
