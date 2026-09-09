package com.netgsm.asterisk.service;

import com.netgsm.asterisk.asterisk.realtime.entity.PsAuth;
import com.netgsm.asterisk.asterisk.realtime.repository.*;
import com.netgsm.asterisk.entity.Endpoint;
import com.netgsm.asterisk.service.provisioning.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EndpointIdentityMigrationTest {
    @Test void legacyAuthPasswordIsPreservedAndOldIdentityRemoved() {
        var naming = mock(AsteriskNaming.class);
        when(naming.endpoint(1L,"1001")).thenReturn("1001-12345");
        when(naming.endpointAuth(1L,"1001")).thenReturn("1001-12345_auth");
        when(naming.tenantPrefix(1L)).thenReturn("tenant1");
        when(naming.safe("1001")).thenReturn("1001");
        when(naming.tenantContext(1L)).thenReturn("tenant_1_internal");
        var endpoints = mock(PsEndpointRepository.class);
        var auths = mock(PsAuthRepository.class);
        var aors = mock(PsAorRepository.class);
        var dialplan = mock(AsteriskExtensionRepository.class);
        var members = mock(AsteriskQueueMemberRepository.class);
        var writer = mock(AsteriskRealtimeWriter.class);
        var service = new AsteriskEndpointProvisioningService(naming,endpoints,auths,aors,dialplan,members,writer);
        var endpoint = new Endpoint(); endpoint.setTenantId(1L); endpoint.setExtension("1001"); endpoint.setEnabled(true);
        var old = new PsAuth(); old.setAuthType("userpass"); old.setPassword("test-secret");
        when(endpoints.existsById("tenant1_1001")).thenReturn(true);
        when(auths.findById("tenant1_1001_auth")).thenReturn(Optional.of(old));
        service.upsert(endpoint,null);
        var migrated = ArgumentCaptor.forClass(PsAuth.class);
        verify(writer).upsertAuth(migrated.capture());
        assertEquals("1001-12345_auth",migrated.getValue().getId());
        assertEquals("1001-12345",migrated.getValue().getUsername());
        assertEquals("test-secret",migrated.getValue().getPassword());
        verify(endpoints).deleteById("tenant1_1001");
        verify(auths).deleteById("tenant1_1001_auth");
        verify(aors).deleteById("tenant1_1001");
        verify(members).deleteAllByStateInterface("PJSIP/tenant1_1001");
        reset(writer);
        when(endpoints.existsById("1001-12345")).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.upsert(endpoint,null));
        verifyNoInteractions(writer);
    }
}
