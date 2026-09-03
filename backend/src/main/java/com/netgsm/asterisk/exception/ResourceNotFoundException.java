package com.netgsm.asterisk.exception;
public class ResourceNotFoundException extends PlatformException {
    public ResourceNotFoundException(String resource) { super(404, "NOT_FOUND", resource + " not found"); }
}
