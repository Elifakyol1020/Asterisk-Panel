package com.netgsm.asterisk.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import jakarta.validation.Validation;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import com.netgsm.asterisk.dto.request.*;
import java.util.*;
class RequestValidationTest {
 @Test void nestedPayloadsRejectNullRowsAndUnsafeIncomingPatterns() {
  try(var factory=Validation.byDefaultProvider().configure().messageInterpolator(new ParameterMessageInterpolator()).buildValidatorFactory()) {
   var validator=factory.getValidator();
   assertFalse(validator.validate(new InboundRouteRequest(null,"test",1L,"_X.","ENDPOINT",1L,true)).isEmpty());
   assertFalse(validator.validate(new InboundRouteRequest(null,"test",1L,"100","TRUNK",1L,true)).isEmpty());
   assertTrue(validator.validate(new InboundRouteRequest(null,"test",1L,"+908501234567","IVR",1L,true)).isEmpty());
   assertTrue(validator.validate(new IvrOptionRequest("#","ENDPOINT",1L)).isEmpty());
   assertFalse(validator.validate(new CreateIvrRequest(null,"menu",null,"welcome",5,3,true,Arrays.asList((IvrOptionRequest)null))).isEmpty());
   assertFalse(validator.validate(new CreateIvrRequest(null,"menu",null,"welcome",5,3,true,List.of(new IvrOptionRequest("12","ENDPOINT",1L)))).isEmpty());
   assertFalse(validator.validate(new CreateQueueRequest(null,"sales","ringall",10,5,0,10,"default",true,List.of(new QueueMemberRequest(1L,-1,false)))).isEmpty());
  }
 }
}
