package com.mlenterman.ssltcpclient;

public class ClientStartupException extends Exception{
    public ClientStartupException(String message){
        super(message);
    }
    
    public ClientStartupException(String message, Throwable cause){
        super(message, cause);
    }
}
