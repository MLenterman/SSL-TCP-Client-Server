package com.mlenterman.ssltcpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicLong;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connection implements Runnable, IConnection{
    private static final Logger logger = LoggerFactory.getLogger(Connection.class);
    private static AtomicLong threadCount = new AtomicLong(0);
    private PrintWriter outputStream;
    private SSLSocket clientSocket;
    private IConnectionHandler connectionHandler;
    private boolean terminate = false;
    
    private InetAddress localIp;
    private InetAddress remoteIp;
    private int localPort;
    private int remotePort;
    
    public Connection(SSLSocket clientSocket, IConnectionHandler connectionHandler){
        this.clientSocket = clientSocket;
        this.localIp = clientSocket.getLocalAddress();
        this.remoteIp = clientSocket.getInetAddress();
        this.localPort = clientSocket.getLocalPort();
        this.remotePort = clientSocket.getPort();
        this.connectionHandler = connectionHandler;
    }
    
    @Override
    public void run(){
        Thread.currentThread().setName("TcpServer - Connection Thread TEST #" + threadCount.incrementAndGet());
        logger.info("Connection thread #" + threadCount.get() + " starting");
        
        System.out.println("Check");
        // Inform the ConnectionHandler
        connectionHandler.connectionEstablished(this);
        
        // Initialize input&output streams
        BufferedReader inputStream;
        try{
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputStream = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }catch(SocketException exception) {
            exception.printStackTrace();
            closeClientSocket();
            return;
        }catch(IOException exception) {
            logger.error("Failed to get streams from client socket", exception);
            closeClientSocket();
            return;
        }
        
        // Process incomming data
        String message = "";
        
        while(!terminate){
            try{  
                message = inputStream.readLine();
                connectionHandler.handleMessage(message, this);

                try{
                    sleep(200);
                }catch(Exception exception){
                    
                }
            }catch(IOException exception){
                logger.warn("Error while reading incomming data", exception);
                closeClientSocket();
                return;
            }
        }  
        closeClientSocket();
    }
    
    public void closeClientSocket(){
        connectionHandler.connectionClosed(this);
        terminate = true;
        try{
            clientSocket.close();
        }catch(Exception exception) {
            logger.warn("Failed to close client socket", exception);
        }
    }
    
    @Override
    public InetAddress getLocalIp(){
        return localIp;
    }
    
    @Override
    public InetAddress getRemoteIp(){
        return remoteIp;
    }
    
    @Override
    public int getLocalPort(){
        return localPort;
    }
    
    @Override
    public int getRemotePort(){
        return remotePort;
    }
    
    @Override
    public synchronized boolean isAlive(){
        return !clientSocket.isClosed();
    }

    @Override
    public void close() {
        terminate = true;
    }
    
    @Override
    public void write(String content){
        System.out.println(content);
        // Make sure a \n is included
        if(content.endsWith("\n"))
            outputStream.print(content);
        else outputStream.println(content);
        
        outputStream.flush();
    }
}
