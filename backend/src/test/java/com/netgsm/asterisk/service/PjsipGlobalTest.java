package com.netgsm.asterisk.service;

import com.netgsm.asterisk.exception.PlatformException;
import jakarta.validation.Validation;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PjsipGlobalTest {
    @Test void missingSchemaAndMultipleRowsAreNotSilentlyOverwritten() {
        var jdbc=mock(JdbcTemplate.class);var service=new PjsipGlobalService(jdbc);
        assertEquals(503, assertThrows(PlatformException.class,service::get).status());
        when(jdbc.queryForObject(anyString(),eq(Boolean.class))).thenReturn(true);
        when(jdbc.queryForList(anyString())).thenReturn(List.of(Map.of("id","one"),Map.of("id","two")));
        assertEquals(409,assertThrows(PlatformException.class,service::get).status());
    }
    @Test void staleRevisionDoesNotWrite() {
        var jdbc=mock(JdbcTemplate.class);var service=new PjsipGlobalService(jdbc);
        when(jdbc.queryForObject(anyString(),eq(Boolean.class))).thenReturn(true);
        when(jdbc.queryForList(anyString())).thenReturn(List.of());
        assertEquals("empty",service.get().revision());
        var settings=new PjsipGlobalService.Settings(null,null,null,null,null,null,null,null);
        assertEquals(409,assertThrows(PlatformException.class,()->service.save(new PjsipGlobalService.Update("stale",settings))).status());
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void requestRejectsUnsafeHeadersAndOutOfRangeNumbers() {
        try(var factory=Validation.byDefaultProvider().configure().messageInterpolator(new ParameterMessageInterpolator()).buildValidatorFactory()) {
            var validator=factory.getValidator();
            assertTrue(validator.validate(new PjsipGlobalService.Settings(null,null,null,null,null,null,null,null)).isEmpty());
            assertFalse(validator.validate(new PjsipGlobalService.Settings("Agent\r\nInjected",256,null,null,-1,"ip,,username",0,null)).isEmpty());
            assertTrue(validator.validate(new PjsipGlobalService.Settings("Panel",70,"asterisk","asterisk",90,"ip,username,anonymous",30,null)).isEmpty());
        }
        assertEquals("hasRole('SUPER_ADMIN')",PjsipGlobalService.class.getAnnotation(PreAuthorize.class).value());
    }
}
