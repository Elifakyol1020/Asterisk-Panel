package com.netgsm.asterisk.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.netgsm.asterisk.repository.*;
import com.netgsm.asterisk.entity.*;
import com.netgsm.asterisk.dto.request.*;
import com.netgsm.asterisk.exception.*;
import com.netgsm.asterisk.service.provisioning.*;
import com.netgsm.asterisk.asterisk.realtime.repository.AsteriskExtensionRepository;
import com.netgsm.asterisk.asterisk.realtime.entity.AsteriskExtension;
import org.mockito.ArgumentCaptor;
import java.util.*;
class InboundRoutingTest {
 private AsteriskNaming naming() {
  var tenants=mock(TenantRepository.class); var tenant=new Tenant();tenant.setId(1L);tenant.setCode("12345");
  when(tenants.findById(1L)).thenReturn(Optional.of(tenant));return new AsteriskNaming(tenants);
 }
 @Test void incomingRouteValidatesTrunkAndTargetWithinTheAuthenticatedTenant() {
  var repository=mock(InboundRouteRepository.class);var current=mock(CurrentUserService.class);var refs=mock(ReferenceService.class);var provisioning=mock(AsteriskDialplanProvisioningService.class);
  when(current.tenantForCreate(999L)).thenReturn(1L);
  var service=new InboundRouteService(repository,current,refs,provisioning,naming());
  var response=service.create(new InboundRouteRequest(999L,"Sales",7L,"+908501234567","ENDPOINT",10L,true));
  assertEquals(1L,response.tenantId());assertEquals("tenant_1_inbound_7",response.context());
  verify(refs).requireTarget(1L,"TRUNK",7L);verify(refs).requireTarget(1L,"ENDPOINT",10L);
  verify(repository).saveAndFlush(any());verify(provisioning).upsertInboundRoute(any());
 }
 @Test void invalidCrossTenantTargetPreventsAnyInsert() {
  var repository=mock(InboundRouteRepository.class);var current=mock(CurrentUserService.class);var refs=mock(ReferenceService.class);var provisioning=mock(AsteriskDialplanProvisioningService.class);
  when(current.tenantForCreate(null)).thenReturn(1L);doThrow(new ResourceNotFoundException("Target")).when(refs).requireTarget(1L,"ENDPOINT",10L);
  var service=new InboundRouteService(repository,current,refs,provisioning,naming());
  assertThrows(ResourceNotFoundException.class,()->service.create(new InboundRouteRequest(null,"Sales",7L,"100","ENDPOINT",10L,true)));
  verify(repository,never()).saveAndFlush(any());verifyNoInteractions(provisioning);
 }
 @Test void duplicateDidOnTheSameTrunkIsRejected() {
  var repository=mock(InboundRouteRepository.class);var current=mock(CurrentUserService.class);var refs=mock(ReferenceService.class);var provisioning=mock(AsteriskDialplanProvisioningService.class);
  when(current.tenantForCreate(null)).thenReturn(1L);when(repository.existsByTenantIdAndTrunkIdAndDidAndIdNot(1L,7L,"100",0L)).thenReturn(true);
  var service=new InboundRouteService(repository,current,refs,provisioning,naming());
  assertThrows(DuplicateResourceException.class,()->service.create(new InboundRouteRequest(null,"Sales",7L,"100","ENDPOINT",10L,true)));verify(repository,never()).saveAndFlush(any());
 }
 @Test void provisionedIncomingNumberDoesNotOverwriteInternalExtension() {
  var realtime=mock(AsteriskExtensionRepository.class);var endpoints=mock(EndpointRepository.class);var writer=mock(AsteriskRealtimeWriter.class);
  var service=new AsteriskDialplanProvisioningService(naming(),mock(InboundRouteRepository.class),realtime,endpoints,mock(QueueRepository.class),mock(IvrRepository.class),mock(TrunkRepository.class),mock(ExtensionRepository.class),mock(IvrOptionRepository.class),writer);
  var endpoint=new Endpoint();endpoint.setTenantId(1L);endpoint.setExtension("1001");when(endpoints.findByIdAndTenantId(10L,1L)).thenReturn(Optional.of(endpoint));
  var route=new InboundRoute();route.setTenantId(1L);route.setTrunkId(7L);route.setDid("1001");route.setTargetType("ENDPOINT");route.setTargetId(10L);route.setEnabled(true);
  service.upsertInboundRoute(route);
  var rows=ArgumentCaptor.forClass(AsteriskExtension.class);verify(writer,times(2)).upsertExtension(rows.capture());
  assertTrue(rows.getAllValues().stream().allMatch(row->row.getContext().equals("tenant_1_inbound_7")));
  assertEquals("PJSIP/1001-12345,30",rows.getAllValues().getFirst().getAppdata());
  verify(realtime,never()).deleteAllByContextAndExten("tenant_1_internal","1001");
  reset(writer);route.setEnabled(false);service.upsertInboundRoute(route);verifyNoInteractions(writer);
 }
}
