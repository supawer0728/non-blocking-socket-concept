package com.parfait.study.nonblocking.domain;

public class ServerSocket extends Socket {

    public ServerSocket(int port, SocketMode mode) {
        super("127.0.0.1", port, mode);
    }
}