# 동기 VS 비동기

## 동기

**소스 코드**

```java
void do() {
  a();
  b();
  c();
}
```

**시간 순서**

```uml
main -> a : 호출
main <- a : 완료
main -> b : 호출
main <- b : 완료
main -> c : 호출
main <- c : 완료
```

## 비동기

**소스 코드**

```java
void do() {
  a();
  b();
  c();
}
```

**시간 순서**

```uml
main -> a : 호출
main -> b : 호출
main -> c : 호출
main <- b : 완료
main <- c : 완료
main <- a : 완료
```

# Blocking VS Non-Blocking

## Blocking

```c
scanf("%s", &input); // 입력이 있을때까지 Thread 대기
printf("%s", input); // 입력을 받은 후에 동작
```

## Non-Blocking

```java
int times = 1;
NonBlockingInput input = new NonBlockingInput(System.in);

// 입력된 값이 있는지 확인
// 대기 하지 않고 바로 return
while(!input.hasMessage()) {
  System.out.println("빨리 입력 하세요!!!");
  times++;
}

System.out.println(times + "번 만에 입력하셨네요 -_- : " + input.getMessage());
```

## Non-Blocking

읽어야할 것이 있는지 없는지 Kernel에 등록하고 필요할 때 확인!

```
NonBlockingInput input = new NonBlockingInput(System.in);
boolean hasMessage = !input.hasMessage()
```

# 네트워크 프로그래밍으로 구현하기

## File Descriptor

Unix 계열의 OS에서는 File Descriptor를 사용한다

```java
abstract class FileDescriptor implements Closeable {
  private final int value; // FileDescriptor는 사실상 int로 표현된다

  abstract public void close();
  abstract public String read();
  abstract public void write();
}
```

OS는 해당 FileDescriptor를 사용해서 표준입출력, 파일IO, 네트워크IO를 처리할 수 있다

**기본 할당**

| 번호 | 내용 | java |
| - | - | - |
| 0 | 표준 입력 | System.in |
| 1 | 표준 출력 | System.out |
| 2 | 표준 오류 출력 | System.err |

> 소켓 또한 FileDescriptor로 취급된다

## Socket과 Kernel(추상화)

### FileDescriptor

```java
class FileDescriptor implements Closeable {
  private final int value; // FileDescriptor는 사실상 int로 표현된다

  public void close();
  public String read();
  public void write();
}
```

### Socket

```java
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

    public enum SocketMode {
        TCP, UDP
    }
}
```

### ServerSocket

```java
public class ServerSocket extends Socket {
    public ServerSocket(int port, SocketMode mode) {
        super("127.0.0.1", port, mode);
    }
}
```

### Kernel

```java
public interface Kernel { 
    // 서버 소켓을 OS에 등록
    // 전화국에 전화번호를 등록
    static void bind(ServerSocket socket) {}

    // 서버 소켓으로부터 요청을 받기 시작
    // 전화선 연결
    static void listen(ServerSocket socket) {}

    // 수신
    // 전화가 울려서 수화기를 듦
    static Socket accept(Socket socket) {
        return Socket.client(socket);
    }
}
```

## Synchronous & Blocking

```java
public class HelloWorldServer {

    public static void main(String[] args) {
        ServerSocket server = new ServerSocket(80, TCP); // FileDescriptor 3번

        // kernel에 socket을 binding하는 작업
        // 동일한 port가 사용중이면 오류 발생
        Kernel.bind(server);

        // 외부 요청이 들어올 경우 수신할 수 있도록 설정
        Kernel.listen(server);
        
        // 외부 요청이 들어올 때까지 block
        Socket client = Kernel.accept(server); 

        // client가 요청을 보낼때까지 block
        System.out.println(client.read()); 
        // client가 응답을 받을때까지 block
        client.write("Hello client!");

        client.close();
        server.close();
    }
}
```

## Asynchronous & Blocking

```java
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
            Socket client = Kernel.accept(server); // 외부 요청이 들어올 때까지 block
            new Thread(new SayHello(client)).start();
        }
        server.close();
    }

    private static class SayHello implements Runnable {

        private final Socket client;

        public SayHello(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            System.out.println(client.read()); // client가 요청을 보낼때까지 block
            client.write("Hello client!"); // client가 응답을 받을때까지 block
            client.close();
        }
    }
}
```

## Asynchronous & Non-Blocking

Unix 계열의 `select` 함수를 예로 설명

```java
public interface Kernel {

    // 현재 구현은 컨셉을 표현한 것으로 실제 구현은 man select로 참조할 것
    static Set<FileDescriptor> select(Collection<FileDescriptor> fileDescriptors) {
        return fileDescriptors.stream()
                              .filter(FileDescriptor::hasReadable)
                              .collect(Collectors.toSet());
    }
}
```

## Asynchronous & Non-Blocking

```java
public class HelloWorldAyncNonBlockingServer {

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
            Set<FileDescriptor> readableSet = Kernel.select(fileDescriptorSet);
            if (readableSet.isEmpty()) {
                continue;
            }

            readableSet.parallelStream().forEach(readable -> process(readable, fileDescriptorSet, server));
        }
        server.close();
    }

    private static void process(FileDescriptor readable, Set<FileDescriptor> fileDescriptorSet, ServerSocket server) {
        if (readable == server) {
            Socket client = Kernel.accept(server);
            fileDescriptorSet.add(client);
            return;
        }

        new Thread(new SayHello(readable)).start();
        // 혹은 ThreadPool 사용
        // threadPool.getThread().run(new SayHello(readable));
    }

    private static class SayHello implements Runnable {

        private final FileDescriptor client;

        public SayHello(FileDescriptor client) {
            this.client = client;
        }

        @Override
        public void run() {
            System.out.println(client.read()); // 읽을 내용이 있으므로 즉시 return
            client.write("Hello client!"); // client가 응답을 받을때까지 block
            client.close();
        }
    }
}
```