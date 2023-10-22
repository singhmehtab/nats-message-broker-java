package org.project;

import lombok.Getter;
import lombok.Setter;
import org.project.parse.Parser;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public class Client {
    @Getter
    private final AsynchronousSocketChannel result;

    // Every client has its own byte buffer which will be used to read messages without creating new memory.
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(4096);

    // Every client has its own parser
    private final Parser parser = new Parser(this);

    @Setter
    @Getter
    private boolean verbose = true;

    public Client(AsynchronousSocketChannel result){
        this.result = result;
    }

    public void startConversation() {
        result.write(ByteBuffer.wrap(("INFO {\"host\":" + "\"" + Server.ip + "\",\"port\":" + Server.PORT +",\"headers\":true,\"max_payload\":1048576,\"jetstream\":true,\"client_id\":9,\"client_ip\":\"127.0.0.1\"}\r\n").getBytes()));
        try {
            int bytesRead = result.read(byteBuffer).get();
            while (bytesRead != -1) {
                // Make the buffer ready to read
                byteBuffer.flip();

                // Convert the buffer into a line
                byte[] lineBytes = new byte[bytesRead];
                byteBuffer.get(lineBytes, 0, bytesRead);
                parser.parse(lineBytes, result);

                // Read the next line
                byteBuffer.clear();
                bytesRead = result.read(byteBuffer).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }




}
