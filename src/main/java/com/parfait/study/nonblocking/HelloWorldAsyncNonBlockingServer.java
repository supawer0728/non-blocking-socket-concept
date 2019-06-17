package com.parfait.study.nonblocking;

import static com.parfait.study.nonblocking.domain.Socket.SocketMode.TCP;

import com.parfait.study.nonblocking.domain.FileDescriptor;
import com.parfait.study.nonblocking.domain.Kernel;
import com.parfait.study.nonblocking.domain.ServerSocket;
import com.parfait.study.nonblocking.domain.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HelloWorldAsyncNonBlockingServer {

    public static void main(String[] args) {
        ServerSocket server = new ServerSocket(80, TCP); // FileDescriptor 3번

        // kernel에 socket을 binding하는 작업
        // 동일한 port가 사용중이면 오류 발생
        Kernel.bind(server);

        // 외부 요청이 들어올 경우 수신할 수 있도록 설정
        Kernel.listen(server);

        Set<FileDescriptor> fileDescriptorSet = new HashSet<>(Arrays.asList(server));

        // 실제 서버에서는 while(true)
        for (int i = 0; i < 1000; i++) {
            Set<FileDescriptor> readableSet = Kernel.epoll(fileDescriptorSet);
            if (readableSet.isEmpty()) {
                continue;
            }

            readableSet.parallelStream().forEach(readable -> process(readable, fileDescriptorSet, server));
        }
        Kernel.close(server);
    }

    private static void process(FileDescriptor readable, Set<FileDescriptor> fileDescriptorSet, ServerSocket server) {
        if (readable == server) {
            Socket client = (Socket) Kernel.accept(server);
            fileDescriptorSet.add(client);
            return;
        }

        new Thread(new SayHello(readable, fileDescriptorSet)).start();
        // 혹은 ThreadPool 사용
        // threadPool.getThread().run(new SayHello(readable));
    }

    private static class SayHello implements Runnable {

        private final FileDescriptor client;
        private final Set<FileDescriptor> fileDescriptorSet;

        public SayHello(FileDescriptor client, Set<FileDescriptor> fileDescriptorSet) {
            this.client = client;
            this.fileDescriptorSet = fileDescriptorSet;
        }

        @Override
        public void run() {
            System.out.println(Kernel.read(client)); // client가 요청을 보낼때까지 block
            Kernel.write(client, "Hello client!");
            Kernel.close(client);
            fileDescriptorSet.remove(client);
        }
    }
}
