package com.parfait.study.nonblocking.domain;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public interface Kernel {

    static Set<FileDescriptor> epoll(Collection<FileDescriptor> fileDescriptors) {
        return fileDescriptors.stream()
                              .filter(Kernel::hasReadable)
                              .collect(Collectors.toSet());
    }

    static boolean hasReadable(FileDescriptor fileDescriptor) {
        return ((int) (Math.random() * 100)) % 5 == 0;
    }

    static void bind(ServerSocket socket) {
    }

    static void listen(ServerSocket socket) {

    }

    static FileDescriptor accept(ServerSocket socket) {
        return Socket.client(socket);
    }

    static void write(FileDescriptor fileDescriptor, String message) {
    }

    static String read(FileDescriptor fileDescriptor) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return "Hello Server!";
    }

    static void close(FileDescriptor fileDescriptor) {
    }
}
