package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.request.CdrInput;
public interface TenantResolver {
    Long resolveTenant(CdrInput input);
}
