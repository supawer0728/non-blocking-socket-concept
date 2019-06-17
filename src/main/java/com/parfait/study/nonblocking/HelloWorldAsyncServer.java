package com.parfait.study.nonblocking;

import static com.parfait.study.nonblocking.domain.Socket.SocketMode.TCP;

import com.parfait.study.nonblocking.domain.Kernel;
import com.parfait.study.nonblocking.domain.ServerSocket;
import com.parfait.study.nonblocking.domain.Socket;

public class HelloWorldAsyncServer {

    public static void main(String[] args) {
        ServerSocket server = new ServerSocket(80, TCP); // FileDescriptor 3번

        // kernel에 socket을 binding하는 작업
        // 동일한 port가 사용중이면 오류 발생
        Kernel.bind(server);

        // 외부 요청이 들어올 경우 수신할 수 있도록 설정
        Kernel.listen(server);

        // 실제 서버에서는 while(true)
        for (int i = 0; i < 1000; i++) {
            Socket client = (Socket) Kernel.accept(server); // 외부 요청이 들어올 때까지 block
            new Thread(new SayHello(client)).start();
        }
        Kernel.close(server);
    }

    private static class SayHello implements Runnable {

        private final Socket client;

        public SayHello(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            System.out.println(Kernel.read(client)); // client가 요청을 보낼때까지 block
            Kernel.write(client, "Hello client!");
            Kernel.close(client);
        }
    }
}
