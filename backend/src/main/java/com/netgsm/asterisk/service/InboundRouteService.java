package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.request.InboundRouteRequest;
import com.netgsm.asterisk.dto.response.InboundRouteResponse;
import com.netgsm.asterisk.entity.InboundRoute;
import com.netgsm.asterisk.repository.InboundRouteRepository;
import com.netgsm.asterisk.exception.*;
import com.netgsm.asterisk.service.provisioning.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.*;
@Service @RequiredArgsConstructor @Transactional
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
public class InboundRouteService {
 private final InboundRouteRepository repository;
 private final CurrentUserService current;
 private final ReferenceService references;
 private final AsteriskDialplanProvisioningService provisioning;
 private final AsteriskNaming naming;
 @Transactional(readOnly=true)
 public Page<InboundRouteResponse> list(Long requestedTenantId, Pageable page) {
  Long tenant=current.tenantForList(requestedTenantId);
  return (tenant==null?repository.findAll(page):repository.findAllByTenantId(tenant,page)).map(this::response);
 }
 @Transactional(readOnly=true) public InboundRouteResponse get(Long id) { return response(find(id)); }
 public InboundRouteResponse create(InboundRouteRequest r) {
  var row=new InboundRoute(); row.setTenantId(current.tenantForCreate(r.tenantId()));
  return save(row,r);
 }
 public InboundRouteResponse update(Long id,InboundRouteRequest r) {
  var row=find(id);current.tenantForCreate(row.getTenantId());
  if(r.tenantId()!=null && !r.tenantId().equals(row.getTenantId())) throw new BusinessRuleException("Route tenant cannot change");
  provisioning.deleteInboundRoute(row); return save(row,r);
 }
 private InboundRouteResponse save(InboundRoute row,InboundRouteRequest r) {
  references.requireTarget(row.getTenantId(),"TRUNK",r.trunkId());
  references.requireTarget(row.getTenantId(),r.targetType(),r.targetId());
  if(repository.existsByTenantIdAndTrunkIdAndDidAndIdNot(row.getTenantId(),r.trunkId(),r.did(),row.getId()==null?0L:row.getId())) throw new DuplicateResourceException("Inbound number on trunk");
  row.setName(r.name());row.setTrunkId(r.trunkId());row.setDid(r.did());row.setTargetType(r.targetType());row.setTargetId(r.targetId());row.setEnabled(r.enabled());
  repository.saveAndFlush(row);provisioning.upsertInboundRoute(row);return response(row);
 }
 public void delete(Long id) {var row=find(id);current.tenantForCreate(row.getTenantId());provisioning.deleteInboundRoute(row);repository.delete(row);repository.flush();}
 private InboundRoute find(Long id) {return (current.isSuperAdmin()?repository.findById(id):repository.findByIdAndTenantId(id,current.getCurrentTenantId())).orElseThrow(()->new ResourceNotFoundException("Inbound route"));}
 private InboundRouteResponse response(InboundRoute r) {return new InboundRouteResponse(r.getId(),r.getTenantId(),r.getName(),r.getTrunkId(),r.getDid(),r.getTargetType(),r.getTargetId(),r.getEnabled(),naming.inboundContext(r.getTenantId(),r.getTrunkId()));}
}
