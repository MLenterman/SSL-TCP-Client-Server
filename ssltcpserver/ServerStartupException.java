package com.mlenterman.ssltcpserver;

public class ServerStartupException extends Exception{
    public ServerStartupException(String message){
        super(message);
    }
    
    public ServerStartupException(String message, Throwable cause){
        super(message, cause);
    }
}
