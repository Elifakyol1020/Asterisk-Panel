package com.netgsm.asterisk.exception;
public class TenantAccessDeniedException extends PlatformException {
    public TenantAccessDeniedException() { super(403, "FORBIDDEN", "Access denied"); }
}
