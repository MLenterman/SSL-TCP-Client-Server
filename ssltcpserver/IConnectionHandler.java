package com.mlenterman.ssltcpserver;

public interface IConnectionHandler{
    void handleMessage(String message, IConnection connection);
    void connectionEstablished(IConnection connection);
    void connectionClosed(IConnection connection);
}
