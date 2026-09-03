package com.netgsm.asterisk.service;

import com.netgsm.asterisk.entity.TenantEntity;
import com.netgsm.asterisk.exception.AsteriskConfigurationException;
import org.springframework.stereotype.Service;

/** Fails closed until the existing Asterisk columns and keys have been inspected.
 * Calls stay inside the feature service transaction so business writes roll back.
 * Do not replace this with guessed mappings or a production no-op implementation.
 */
@Service
public class AsteriskRealtimeService {
    public void save(TenantEntity entity) { throw new AsteriskConfigurationException(); }
    public void save(TenantEntity entity, String password) { throw new AsteriskConfigurationException(); }
    public void delete(TenantEntity entity) { throw new AsteriskConfigurationException(); }
}
