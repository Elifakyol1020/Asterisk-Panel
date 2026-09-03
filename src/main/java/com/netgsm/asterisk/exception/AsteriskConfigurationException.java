package com.netgsm.asterisk.exception;
public class AsteriskConfigurationException extends PlatformException {
    public AsteriskConfigurationException() { super(503, "ASTERISK_UNAVAILABLE", "Asterisk schema integration has not been configured"); }
}
