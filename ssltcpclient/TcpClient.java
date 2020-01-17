package com.mlenterman.ssltcpclient;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpClient{  
    private static final Logger logger = LoggerFactory.getLogger(TcpClient.class);
    private InetAddress remoteIp;
    private int port;
    private SSLSocket clientSocket;
    private ExecutorService executorService;
    private IConnectionHandler connectionHandler;
    private boolean terminate = false;
    
    public TcpClient(InetAddress remoteIp, int port, ExecutorService executorService, IConnectionHandler connectionHandler){
        this.remoteIp = remoteIp;
        this.port = port;
        this.executorService = executorService;
        this.connectionHandler = connectionHandler;
    }
    
    public TcpClient(InetAddress remoteIp, int port, IConnectionHandler connectionHandler){
        this.remoteIp = remoteIp;
        this.port = port;
        this.executorService = Executors.newCachedThreadPool();
        this.connectionHandler = connectionHandler;
    }
    
    public void start()throws ClientStartupException{
        logger.info("TcpClient starting");
        
        // Try to create a socket
        SSLSocketFactory socketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        try{
            clientSocket = (SSLSocket)socketFactory.createSocket(remoteIp, port);
        }catch (IOException exception){
            throw new ClientStartupException("Failed to create client socket!", exception);
        }    
        clientSocket.setUseClientMode(true);
        System.out.println("connected: " + clientSocket.isConnected());
        // Start connection thread
        executorService.submit(new Connection(clientSocket, connectionHandler));
    }
    
    public void shutdown(long millis)throws IOException, InterruptedException{
        terminate = true;
        clientSocket.close();
        executorService.shutdown();
        
        if(!executorService.awaitTermination(millis, TimeUnit.MILLISECONDS)){
            List<Runnable> unfinishedConnections = executorService.shutdownNow();
            
            for(Runnable job : unfinishedConnections)
                ((Connection)job).closeClientSocket();
        }
    }
}
