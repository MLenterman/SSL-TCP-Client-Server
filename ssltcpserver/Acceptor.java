package com.mlenterman.ssltcpserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Acceptor implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(Acceptor.class);
    private boolean terminate = false;   
    private SSLServerSocket serverSocket;
    private ExecutorService executorService;
    private IConnectionHandler connectionHandler;
    
    public Acceptor(SSLServerSocket serverSocket, ExecutorService executorService, IConnectionHandler connectionHandler)throws ServerStartupException{
        this.serverSocket = serverSocket;
        this.executorService = executorService;
        this.connectionHandler = connectionHandler;
    }
    
    @Override
    public void run(){
        Thread.currentThread().setName("TcpServer - Acceptor Thread");
        logger.info("Acceptor thread starting");
        
        while(!terminate){
            // Accept client sockets
            SSLSocket clientSocket;
            try{
                clientSocket = (SSLSocket)serverSocket.accept();
            }catch(SocketException exception){
                logger.info("Shutdown by server");
                return;
            }catch(Exception exception){
                logger.error("Error during serverSocket.accept()", exception);
                return;
            }
            
            // Debug
            System.out.println("Socket class: "+clientSocket.getClass());
            System.out.println("   Remote address = "
               +clientSocket.getInetAddress().toString());
            System.out.println("   Remote port = "+clientSocket.getPort());
            System.out.println("   Local socket address = "
               +clientSocket.getLocalSocketAddress().toString());
            System.out.println("   Local address = "
               +clientSocket.getLocalAddress().toString());
            System.out.println("   Local port = "+clientSocket.getLocalPort());
            System.out.println("   Need client authentication = "
               +clientSocket.getNeedClientAuth());
            SSLSession ss = clientSocket.getSession();
            System.out.println("   Cipher suite = "+ss.getCipherSuite());
            System.out.println("   Protocol = "+ss.getProtocol());
            
            
            // Start worker thread
            try{
                executorService.submit(new Connection(clientSocket, connectionHandler));
            }catch(RejectedExecutionException exception){
                if(executorService.isShutdown())
                    return;
                try{
                    clientSocket.close();
                }catch(IOException exception1){
                    logger.warn("Failed to close clientSocket!", exception1);
                }
            }catch(NullPointerException exception){
                logger.error("NullPointerException in a worker!", exception);
                return;
            }
        }
        logger.info("Acceptor thread stopped");
    }
}
