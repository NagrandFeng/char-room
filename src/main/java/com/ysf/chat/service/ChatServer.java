package com.ysf.chat.service;

import com.ysf.chat.entity.User;
import com.ysf.chat.utils.UserTalkUtils;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author nagrand
 * @title
 * @date 2020/4/25
 */
public class ChatServer {

    private Set<User> users;

    private Map<String, User> userMap;

    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private Selector selector;


    public ChatServer() {
        try {
            init();
        } catch (Exception e) {
            System.out.println("初始化失败");
            e.printStackTrace();
        }
    }

    private void init() throws Exception {
        userMap = new HashMap<>();
        users = new HashSet<>();
        readBuffer = ByteBuffer.allocate(1024);
        writeBuffer = ByteBuffer.allocate(1024);
        selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(8081));

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("聊天室初始化完成...");
    }

    private void serverStart() {

        try {
            while (selector.select() > 0) {
                Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                while (selectionKeys.hasNext()) {
                    SelectionKey key = selectionKeys.next();
                    selectionKeys.remove();
                    handleKey(key);
                }
            }
            selector.selectedKeys().clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleKey(SelectionKey key) throws Exception{
        if (key.isAcceptable()) {
            accept(key);
        }

        if (key.isReadable()) {
            processContent(key);
        }
    }
    private void accept(SelectionKey key) throws Exception {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector,SelectionKey.OP_READ);
        System.out.println("一个新的连接建立了:"+client.getRemoteAddress());
        broadCast(null,"当前有"+(users.size()+1) +"人\n");
        write(client,"欢迎来到聊天室，请输入昵称");
        key.interestOps(SelectionKey.OP_ACCEPT);
    }

    private void processContent(SelectionKey key) throws IOException {

        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            String receiveData = receive(socketChannel);
            if(receiveData.contains(UserTalkUtils.USER_LOGIN_TAG)){
                String userName = receiveData.replace(UserTalkUtils.USER_LOGIN_TAG,"");
                if(userMap.containsKey(userName)){
                    write(socketChannel,"用户名:"+userName +"已经存在");
                } else {
                    User user = new User();
                    user.setLogin(true);
                    user.setUserName(userName);
                    user.setRemoteAddress(socketChannel.getRemoteAddress().toString());
                    users.add(user);
                    userMap.put(user.getRemoteAddress(),user);
                    write(socketChannel,UserTalkUtils.LOGIN_SUCCESS_MESSAGE+userName);
                }
            } else if(receiveData.equals("list")){
                StringBuilder sb = new StringBuilder();
                sb.append("当前用户列表:\n");
                users.forEach(u ->{
                    sb.append(u.getUserName()).append("\n\r");
                });
                String content =  sb.toString();
                write(socketChannel,content);
            } else if(receiveData.equals("my-history")){
                String remoteAddress = socketChannel.getRemoteAddress().toString();
                User current = userMap.get(remoteAddress);

                StringBuilder sb = new StringBuilder();
                sb.append("用户"+current.getUserName()+"的聊天记录如下:\n");
                current.getHistory().forEach(h ->{
                    sb.append(h).append("\n\r");
                });
                String content =  sb.toString();
                write(socketChannel,content);
            } else {
                String remoteAddress = socketChannel.getRemoteAddress().toString();
                User currentTalking = userMap.get(remoteAddress);
                userMap.get(remoteAddress).addTalkingHistory(receiveData);
                broadCast(socketChannel,currentTalking.getUserName() + " 说："+ receiveData);
            }

        } catch (Exception e) {
            String address = socketChannel.getRemoteAddress().toString();
            User disConnectUser = userMap.get(address);
            String name = disConnectUser.getUserName();
            System.out.println(name + "断开了链接");

            users.remove(disConnectUser);
            userMap.remove(address);
            socketChannel.close();
            broadCast(null,name + " 离开了聊天室!" + "还剩" + userMap.size() + "人!" );
        }
    }



    private String receive(SocketChannel channel) throws IOException{
        readBuffer.clear();
        int count = channel.read(readBuffer);
        return  new String(readBuffer.array(),0,count,StandardCharsets.UTF_8);
    }

    private void write(SocketChannel channel, String content) throws IOException {
        writeBuffer.clear();
        writeBuffer.put(content.getBytes(StandardCharsets.UTF_8));
        writeBuffer.flip();
        channel.write(writeBuffer);
    }

    private void broadCast(SocketChannel except,String content) throws IOException{
        for (SelectionKey key: selector.keys()) {
            Channel targetChannel = key.channel();
            //向除了发送方以外的channel 发送数据
            if(targetChannel.isOpen() && targetChannel instanceof SocketChannel && targetChannel != except) {
                write((SocketChannel) targetChannel, content);
            }
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.serverStart();
    }



}
