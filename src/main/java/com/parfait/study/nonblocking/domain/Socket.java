package com.parfait.study.nonblocking.domain;

public class Socket extends FileDescriptor {

    private final String address;
    private final int port;
    private final SocketMode mode;

    public Socket(String address, int port, SocketMode mode) {
        super();
        this.address = address;
        this.port = port;
        this.mode = mode;
    }

    public static Socket client(Socket socket) {
        return new Socket("x.x.x.x", (int) (Math.random() * 10000), socket.mode);
    }

    public enum SocketMode {
        TCP, UDP
    }
}
