package com.mlenterman.ssltcpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connection implements Runnable, IConnection{
    private static final Logger logger = LoggerFactory.getLogger(Connection.class);
    private static AtomicLong threadCount = new AtomicLong(0);
    private OutputStreamWriter outputStream;
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
        Thread.currentThread().setName("TcpServer - Connection Thread #" + threadCount.incrementAndGet());
        logger.info("Connection thread #" + threadCount.get() + " starting");
        
        /*
        try{
            System.out.println("Before handshake");
            System.out.println("connected: " + clientSocket.isConnected());
            clientSocket.startHandshake();
            System.out.println("After handshake");
        }catch(IOException ex){
            logger.error("Handshake failed", ex);      
        }
        */
        // Debug
        System.out.println("Before debug");
        SSLSession session = clientSocket.getSession();
        logger.info(session.getCipherSuite());
        logger.info(session.getProtocol());
        logger.info("Valid: " + session.isValid());
        System.out.println("After debug");
        
        // Initialize input&output streams
        BufferedReader inputStream;
        try{
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputStream = new OutputStreamWriter(clientSocket.getOutputStream());
            //outputStream.write("test\n");
            //outputStream.flush();
        }catch(SocketException exception){
            logger.error("SocketException", exception);
            closeClientSocket();
            return;
        }catch(IOException exception){
            logger.error("Failed to get streams from client socket", exception);
            closeClientSocket();
            return;
        }
        
        // Inform the ConnectionHandler
        connectionHandler.connectionEstablished(this);

        // Process incomming data
        while(!terminate){
            try{  
                connectionHandler.handleMessage(inputStream.readLine(), this);
                
                try{
                    sleep(200);
                }catch(Exception exception){
                    logger.error("IO error in connection thread", exception);
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
    public synchronized void write(String content){  
        try{
            if(content.endsWith("\n"))
                outputStream.write(content);
            else outputStream.write(content + "\n");
            outputStream.flush();
        }catch(IOException ex){
            logger.error("Failed to write to OutputStream", ex);
        }
    }
}
