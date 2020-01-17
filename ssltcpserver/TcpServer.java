package com.mlenterman.ssltcpserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpServer{  
    private static final Logger logger = LoggerFactory.getLogger(TcpServer.class);
    private int maxConnections;
    private int port;
    private InetAddress bindIp;
    private SSLServerSocket serverSocket;
    private ExecutorService executorService;
    private Thread acceptor;
    private IConnectionHandler connectionHandler;
    private boolean terminate = false;
    
    private String ksName = "ServerJKS.jks";
    private char ksPass[] = "ServerPass".toCharArray();
    private char ctPass[] = "BankStel".toCharArray();
    
    public TcpServer(InetAddress bindIp, int port, ExecutorService executorService, IConnectionHandler connectionHandler){
        this.bindIp = bindIp;
        this.port = port;
        this.executorService = executorService;
        this.connectionHandler = connectionHandler;
    }
    
    public TcpServer(int port, ExecutorService executorService, IConnectionHandler connectionHandler){
        this.port = port;
        this.executorService = executorService;
        this.connectionHandler = connectionHandler;
    }
    
    public TcpServer(int port, IConnectionHandler connectionHandler){
        this.port = port;
        this.executorService = Executors.newCachedThreadPool();
        this.connectionHandler = connectionHandler;
    }
    
    public void start()throws ServerStartupException{
        logger.info("TcpServer starting");   
        
        // Try to create a serverSocket
        try{
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(ksName), ksPass);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, ctPass);
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), null, null);
         
            SSLServerSocketFactory serverSocketFactory = sc.getServerSocketFactory();
            serverSocket = (SSLServerSocket)serverSocketFactory.createServerSocket(port); 
            printServerSocketInfo(serverSocket);
        }catch (Exception exception){
            throw new ServerStartupException("Failed to create server socket!", exception);
        }    
        
        // Start acceptor thread
        acceptor = new Thread(new Acceptor(serverSocket, executorService, connectionHandler));
        acceptor.start();
    }
    
    public void shutdown(long millis)throws IOException, InterruptedException{
        terminate = true;
        serverSocket.close();
        acceptor.join();
        executorService.shutdown();
        
        if(!executorService.awaitTermination(millis, TimeUnit.MILLISECONDS)){
            List<Runnable> unfinishedWorkers = executorService.shutdownNow();
            
            for(Runnable job : unfinishedWorkers)
                ((Connection)job).closeClientSocket();
        }
    }
    
    private void printServerSocketInfo(SSLServerSocket serverSocket){
      System.out.println("Server socket class: "+serverSocket.getClass());
      System.out.println("   Socker address = "
         +serverSocket.getInetAddress().toString());
      System.out.println("   Socker port = "
         +serverSocket.getLocalPort());
      System.out.println("   Need client authentication = "
         +serverSocket.getNeedClientAuth());
      System.out.println("   Want client authentication = "
         +serverSocket.getWantClientAuth());
      System.out.println("   Use client mode = "
         +serverSocket.getUseClientMode());
   } 
}
