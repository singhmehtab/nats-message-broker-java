package org.project;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Server {
    public static final Integer PORT = 4222;
    public static final String ip = "127.0.0.1";

    public Server(){
        try(AsynchronousServerSocketChannel listener
                    = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(ip, PORT));) {
            while (true){
                listener.accept(null, new CompletionHandler<>() {
                    @Override
                    public void completed(AsynchronousSocketChannel result, Object attachment) {
                        if (listener.isOpen()) {
                            listener.accept(null, this);
                        }
                        Client client = new Client(result);
                        client.startConversation();
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {

                    }
                });
                System.in.read();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
