package com.ysf.chat.service;

import com.ysf.chat.utils.UserTalkUtils;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author nagrand
 * @title
 * @date 2020/4/25
 */
public class UserClient {

    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private Selector selector;
    private final InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8081);
    SocketChannel client;
    public boolean success = false;

    private String name = "";

    public UserClient() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception{
        readBuffer = ByteBuffer.allocate(1024);
        writeBuffer = ByteBuffer.allocate(1024);
        selector = Selector.open();

        client = SocketChannel.open(address);//连接到服务器
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);

    }

    private String receive(SocketChannel channel) throws IOException {
        readBuffer.clear();
        int count = channel.read(readBuffer);
        return  new String(readBuffer.array(),0,count, StandardCharsets.UTF_8);
    }


    private void write(SocketChannel channel, String content) throws IOException {
        writeBuffer.clear();
        writeBuffer.put(content.getBytes(StandardCharsets.UTF_8));
        writeBuffer.flip();
        channel.write(writeBuffer);
    }

    public void clientStart() throws IOException {
        new Thread(()->{
            try {
                while (true){
                    selector.select();
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                    while (keyIterator.hasNext()){
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        handleKey(key);
                    }
                    selectionKeys.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        String tmp = null;
        //等待登录
        while (true){
            tmp = scanner.nextLine();
            if(success){
                break;
            }
            write(client,tmp+ UserTalkUtils.USER_LOGIN_TAG);
        }
        //登录
        write(client,tmp);
        System.out.println(name + "说："+tmp);
        while (true){
            String msg = scanner.nextLine();
            if (!msg.trim().equals("")){
                write(client, msg);
                System.out.println(name + "说："+msg);
            }
        }

    }

    private void handleKey(SelectionKey key) throws IOException{
        //只关心读
        if(key.isReadable()){
            SocketChannel client = (SocketChannel)key.channel();
            String receivedData = receive(client);
            if(receivedData.contains(UserTalkUtils.LOGIN_SUCCESS_MESSAGE)){
                name = receivedData.substring(UserTalkUtils.LOGIN_SUCCESS_MESSAGE.length());
                success = true;
            }
            System.out.println(receivedData);
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    public static void main(String[] args) throws Exception {
        UserClient client  = new UserClient();
        client.clientStart();
    }

}
