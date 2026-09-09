package com.netgsm.asterisk.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.netgsm.asterisk.repository.*;
import com.netgsm.asterisk.entity.*;
import com.netgsm.asterisk.mapper.*;
import com.netgsm.asterisk.dto.request.*;
import com.netgsm.asterisk.exception.*;
import com.netgsm.asterisk.service.provisioning.*;
import java.util.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
class CombinedCreateTest {
 @Test void ivrAndDigitTargetsAreCreatedWithinOneTransaction() {
  var current=mock(CurrentUserService.class);when(current.tenantForCreate(null)).thenReturn(1L);
  var repo=mock(IvrRepository.class);when(repo.saveAndFlush(any())).thenAnswer(inv->{Ivr row=inv.getArgument(0);row.setId(3L);return row;});
  var options=mock(IvrOptionService.class);var service=new IvrService(new IvrMapper(),repo,current,mock(ReferenceService.class),options,mock(AsteriskDialplanProvisioningService.class));
  var transaction=mock(PlatformTransactionManager.class);var status=mock(TransactionStatus.class);when(transaction.getTransaction(any())).thenReturn(status);
  var factory=new ProxyFactory(service);factory.addAdvice(new TransactionInterceptor(transaction,new AnnotationTransactionAttributeSource()));var proxy=(IvrService)factory.getProxy();
  var choice=new IvrOptionRequest("1","ENDPOINT",11L);
  proxy.create(new CreateIvrRequest(null,"Main",null,"welcome",5,3,true,List.of(choice)));
  verify(options).create(3L,choice);verify(transaction).commit(status);verify(transaction,never()).rollback(any());
 }
 @Test void invalidQueueMemberRollsBackParentCreate() {
  var current=mock(CurrentUserService.class);when(current.tenantForCreate(null)).thenReturn(1L);
  var repo=mock(QueueRepository.class);when(repo.saveAndFlush(any())).thenAnswer(inv->{com.netgsm.asterisk.entity.Queue row=inv.getArgument(0);row.setId(3L);return row;});
  var members=mock(QueueMemberService.class);doThrow(new ResourceNotFoundException("Endpoint")).when(members).create(eq(3L),any());
  var service=new QueueService(new QueueMapper(),repo,current,mock(ReferenceService.class),mock(AsteriskDialplanProvisioningService.class),members,mock(AsteriskQueueProvisioningService.class));
  var transaction=mock(PlatformTransactionManager.class);var status=mock(TransactionStatus.class);when(transaction.getTransaction(any())).thenReturn(status);
  var factory=new ProxyFactory(service);factory.addAdvice(new TransactionInterceptor(transaction,new AnnotationTransactionAttributeSource()));var proxy=(QueueService)factory.getProxy();
  assertThrows(ResourceNotFoundException.class,()->proxy.create(new CreateQueueRequest(null,"sales","ringall",20,5,0,10,"default",true,List.of(new QueueMemberRequest(999L,0,false)))));
  verify(transaction).rollback(status);verify(transaction,never()).commit(any());
 }
}
