# chat-room
NIO-DEMO

## 简介
### ChatServer 
聊天室服务器端，提供

1.广播从客户端接收的消息

2.用户列表

3.聊天记录信息


### UserClient 
聊天室客户端

1.连接服务端

2.发送消息

## 关于NIO的主要组件

### Buffer
缓冲区 

本质上就是是一块可以写入数据，然后可以从中读取数据的内存

缓冲区和通道之间进行数据交互

### Channel
通道

用于在字节缓冲区和位于通道另一侧的实体，通常是一个文件或套接字之间有效地传输数据

### Selector 
#### 概念
选择器提供用于监视一个或多个NIO通道并识别何时其中的Channel可用于数据传输的机制。

#### 为什么使用
使用一个线程而不是几个来管理多个通道。线程之间的上下文切换对于操作系统来说是昂贵的，此外，每个线程也占用内存；并且，选择器即可以读取数据; 还可以监听传入的网络连接并通过通道写入数据。

#### 四种事件
OP_CONNECT ：当客户端尝试连接到服务器时。
OP_ACCEPT：当服务器接受来自客户端的连接时。
OP_READ：服务器准备好从通道读取时。
OP_WRITE：服务器准备好写入通道时。

### ServerSocketChannel
用于监听TCP链接请求的通道，好比网络编程中的ServerSocket

### SocketChannel
SocketChannel是用于TCP网络连接的套接字接口，相当于网络编程中的Socket套接字接口。

创建SocketChannel如下：
打开一个SocketChannel并连接网络上的一台服务器。
当ServerSocketChannel接收到一个连接请求时，会创建一个SocketChannel。

