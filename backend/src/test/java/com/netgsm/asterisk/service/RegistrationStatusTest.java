package com.netgsm.asterisk.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.net.*;
import java.io.*;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
class RegistrationStatusTest {
 void field(Object target,String name,Object value) throws Exception {var f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.set(target,value);}
 AsteriskRegistrationService service(int port) throws Exception {
  var service=new AsteriskRegistrationService();field(service,"host","127.0.0.1");field(service,"port",port);field(service,"username","test");field(service,"secret","test");return service;
 }
 void request(BufferedReader reader) throws Exception {while(!reader.readLine().isEmpty()){} }
 @Test void usesExpiryAndDoesNotTreatPermanentOrExpiredContactsAsRegistration() throws Exception {
  try(var server=new ServerSocket(0);var executor=Executors.newSingleThreadExecutor()) {
   var fake=executor.submit(()->{try(var socket=server.accept()) {
    socket.setSoTimeout(3000);var reader=new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));var writer=new PrintWriter(socket.getOutputStream(),true,StandardCharsets.UTF_8);
    writer.print("Asterisk Call Manager/11.0\r\n");writer.flush();request(reader);
    writer.print("Response: Success\r\nActionID: login\r\n\r\n");writer.flush();request(reader);
    for(var item:new String[][]{{"tenant1_1001",String.valueOf(Instant.now().getEpochSecond()+3600)},{"tenant1_1002","1"},{"tenant1_trunk","0"}}) writer.print("Event: ContactList\r\nActionID: contacts\r\nEndpoint: "+item[0]+"\r\nExpirationTime: "+item[1]+"\r\n\r\n");
    writer.print("Event: ContactListComplete\r\nActionID: contacts\r\n\r\n");writer.flush();return true;
   }catch(Exception e){throw new RuntimeException(e);}});
   var contacts=service(server.getLocalPort()).registeredContacts();assertNotNull(contacts);assertEquals(java.util.Set.of("tenant1_1001"),contacts.keySet());assertTrue(fake.get(5,TimeUnit.SECONDS));
  }
 }
 @Test void noContactsIsUnregisteredRatherThanUnknown() throws Exception {
  try(var server=new ServerSocket(0);var executor=Executors.newSingleThreadExecutor()) {
   var fake=executor.submit(()->{try(var socket=server.accept()) {
    socket.setSoTimeout(3000);var reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));var writer=new PrintWriter(socket.getOutputStream(),true);
    writer.print("Asterisk Call Manager/11.0\r\n");writer.flush();request(reader);writer.print("Response: Success\r\nActionID: login\r\n\r\n");writer.flush();request(reader);
    writer.print("Response: Error\r\nActionID: contacts\r\nMessage: No Contacts found\r\n\r\n");writer.flush();return true;
   }catch(Exception e){throw new RuntimeException(e);}});
   assertEquals(java.util.Map.of(),service(server.getLocalPort()).registeredContacts());assertTrue(fake.get(5,TimeUnit.SECONDS));
  }
 }
 @Test void connectionFailureIsUnknown() throws Exception {int port;try(var server=new ServerSocket(0)){port=server.getLocalPort();}assertNull(service(port).registeredContacts());}
}
