package com.netgsm.asterisk.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class AsteriskRegistrationService {
 @Value("${app.asterisk.ami.host:}") private String host;
 @Value("${app.asterisk.ami.port:5038}") private int port;
 @Value("${app.asterisk.ami.username:}") private String username;
 @Value("${app.asterisk.ami.secret:}") private String secret;
 private long checkedAt;
 private Map<String,Long> contacts;
 public synchronized Map<String,Long> registeredContacts() {
  if(System.currentTimeMillis()-checkedAt<4000) return contacts;
  try { contacts=fetch(); } catch(IOException | RuntimeException ex) { contacts=null; }
  checkedAt=System.currentTimeMillis();return contacts;
 }
 private Map<String,Long> fetch() throws IOException {
  if(host.isBlank()||username.isBlank()||secret.isBlank()) throw new IOException("AMI is not configured");
  if(username.contains("\r")||username.contains("\n")||secret.contains("\r")||secret.contains("\n")) throw new IOException("Invalid AMI credentials");
  long deadline=System.nanoTime()+3_000_000_000L;
  try(Socket socket=new Socket()) {
   socket.connect(new InetSocketAddress(host,port),1500);socket.setSoTimeout(1500);
   var reader=new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));
   var writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8));
   reader.readLine();
   writer.write("Action: Login\r\nActionID: login\r\nUsername: "+username+"\r\nSecret: "+secret+"\r\nEvents: off\r\n\r\n");writer.flush();
   Map<String,String> frame;
   do {frame=readFrame(reader,deadline);} while(!"login".equals(frame.get("ActionID")));
   if(!"Success".equals(frame.get("Response"))) throw new IOException("AMI login failed");
   writer.write("Action: PJSIPShowContacts\r\nActionID: contacts\r\n\r\n");writer.flush();
   Map<String,Long> result=new HashMap<>();
   while(true) {
    frame=readFrame(reader,deadline);
    if(!"contacts".equals(frame.get("ActionID"))) continue;
    if("Error".equals(frame.get("Response"))) {
     if("No Contacts found".equals(frame.get("Message"))) return Map.of();
     throw new IOException("AMI contacts unavailable");
    }
    if("ContactListComplete".equals(frame.get("Event"))) return Map.copyOf(result);
    if("ContactList".equals(frame.get("Event"))) {
     long expires=Long.parseLong(frame.getOrDefault("ExpirationTime","0"));
     String endpoint=frame.getOrDefault("Endpoint", "");
     if(!endpoint.isBlank() && expires>Instant.now().getEpochSecond()) result.merge(endpoint,expires,Math::max);
    }
   }
  }
 }
 private Map<String,String> readFrame(BufferedReader reader,long deadline) throws IOException {
  Map<String,String> values=new HashMap<>();int size=0;
  while(true) {
   if(System.nanoTime()>deadline) throw new IOException("AMI timed out");
   String line=reader.readLine();if(line==null) throw new EOFException();
   if(line.isEmpty()) {if(!values.isEmpty()) return values;continue;}
   if((size+=line.length())>65536) throw new IOException("AMI frame too large");
   int separator=line.indexOf(':');if(separator>0) values.put(line.substring(0,separator),line.substring(separator+1).trim());
  }
 }
}
