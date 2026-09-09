package com.netgsm.asterisk.service;

import com.netgsm.asterisk.dto.request.*;
import com.netgsm.asterisk.entity.Tenant;
import com.netgsm.asterisk.enums.TenantStatus;
import com.netgsm.asterisk.exception.PlatformException;
import com.netgsm.asterisk.mapper.TenantMapper;
import com.netgsm.asterisk.repository.TenantRepository;
import com.netgsm.asterisk.service.provisioning.AsteriskNaming;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantNumberTest {
    private Tenant tenant(long id, String code) {
        var t = new Tenant(); t.setId(id); t.setCode(code); return t;
    }

    @Test void sameExtensionHasDistinctSipIdentityAndLeadingZerosArePreserved() {
        var repo = mock(TenantRepository.class);
        when(repo.findById(1L)).thenReturn(Optional.of(tenant(1, "01234")));
        when(repo.findById(2L)).thenReturn(Optional.of(tenant(2, "12345")));
        var naming = new AsteriskNaming(repo);
        assertEquals("1001-01234", naming.endpoint(1L, "1001"));
        assertEquals("1001-12345_auth", naming.endpointAuth(2L, "1001"));
        assertEquals("tenant_1_internal", naming.tenantContext(1L));
    }

    @Test void numberCannotChangeAndInvalidLengthsAreRejected() {
        var repo = mock(TenantRepository.class);
        when(repo.findById(1L)).thenReturn(Optional.of(tenant(1, "12345")));
        var service = new TenantService(new TenantMapper(), repo);
        assertThrows(PlatformException.class, () -> service.update(1L, new TenantRequest("Test", "54321", TenantStatus.ACTIVE)));
        for (String invalid : new String[]{"123", "1234567", "abcd"})
            assertThrows(PlatformException.class, () -> service.create(new TenantRequest("Test", invalid, TenantStatus.ACTIVE)));
        verify(repo, never()).saveAndFlush(any());
    }

    @Test void cdrResolvesNewIdentityAndRejectsCrossTenantSignals() {
        var repo = mock(TenantRepository.class);
        when(repo.findByCode("01234")).thenReturn(Optional.of(tenant(1, "01234")));
        when(repo.findByCode("54321")).thenReturn(Optional.of(tenant(2, "54321")));
        when(repo.existsById(1L)).thenReturn(true);
        var resolver = new ContextTenantResolver(repo);
        assertEquals(1L, resolver.resolveTenant(input(null, "PJSIP/1001-01234-000000ab", null)));
        assertEquals(1L, resolver.resolveTenant(input("tenant_1_internal", "PJSIP/tenant1_1001-0001", null)));
        assertThrows(PlatformException.class, () -> resolver.resolveTenant(input("tenant_1_internal", "PJSIP/1001-54321-000000ab", null)));
        assertThrows(PlatformException.class, () -> resolver.resolveTenant(input(null, "PJSIP/1001-01234-000000ab", "PJSIP/1002-54321-000000ac")));
        assertThrows(PlatformException.class, () -> resolver.resolveTenant(input("tenant_1_internal", "PJSIP/1001-99999-0001", null)));
    }

    private CdrInput input(String context, String channel, String dst) {
        return new CdrInput("1", "1", 0, "1001", "1002", null, null, context, channel, dst, null, null, null, 0, 0, "BUSY", null);
    }
}
