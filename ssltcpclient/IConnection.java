package com.mlenterman.ssltcpclient;

import java.net.InetAddress;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public interface IConnection{
    InetAddress getLocalIp();
    InetAddress getRemoteIp();
    int getLocalPort();
    int getRemotePort();
    
    void write(String content);
    boolean isAlive();
    void close();
    
    static void printSocketInfo(SSLSocket socket){
      System.out.println("Socket class: "+socket.getClass());
      System.out.println("   Remote address = "
         +socket.getInetAddress().toString());
      System.out.println("   Remote port = "+socket.getPort());
      System.out.println("   Local socket address = "
         +socket.getLocalSocketAddress().toString());
      System.out.println("   Local address = "
         +socket.getLocalAddress().toString());
      System.out.println("   Local port = "+socket.getLocalPort());
      System.out.println("   Need client authentication = "
         +socket.getNeedClientAuth());
      SSLSession session = socket.getSession();
      System.out.println("   Cipher suite = "+session.getCipherSuite());
      System.out.println("   Protocol = "+session.getProtocol());
   }
}
