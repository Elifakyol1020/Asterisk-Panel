package com.netgsm.asterisk.exception;
public class BusinessRuleException extends PlatformException {
    public BusinessRuleException(String message) { super(400, "BUSINESS_RULE", message); }
}
