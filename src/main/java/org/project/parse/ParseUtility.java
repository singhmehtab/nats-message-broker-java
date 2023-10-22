package org.project.parse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.project.dto.PubArgs;
import org.project.dto.SubArgs;
import org.project.dto.UnsubArgs;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ParseUtility {

    public static final ByteBuffer OK_MESSAGE = ByteBuffer.wrap("+OK\r\n".getBytes());

    public static final ByteBuffer PONG_MESSAGE = ByteBuffer.wrap("PONG\r\n".getBytes());

    public static final ByteBuffer INVALID_MESSAGE = ByteBuffer.wrap("-ERR 'Unknown Protocol Operation'\r\n".getBytes());

    //TODO Complete these methods to accept all the parameters as nats server
    public static SubArgs getSubArgs(byte[] buffer){
        SubArgs subArgs = SubArgs.builder().build();
        int argStart = 0;
        int argNumber = 0;
        for(int i=0;i<buffer.length;i++){
            switch (buffer[i]){
                case (byte) ' ', (byte) '\t' -> {
                    if(argNumber == 0) subArgs.setTopic(Arrays.copyOfRange(buffer, argStart, i));
                    else if(argNumber == 1) subArgs.setSubId(Arrays.copyOfRange(buffer, argStart, i));
                    argStart = i + 1;
                    argNumber++;
                }
            }
        }
        if(argNumber == 1) subArgs.setSubId(Arrays.copyOfRange(buffer, argStart, buffer.length));
        return subArgs;
    }

    public static PubArgs getPubArgs(byte[] buffer){
        PubArgs pubArgs = PubArgs.builder().build();
        int argStart = 0;
        int argNumber = 0;
        for(int i=0;i<buffer.length;i++){
            switch (buffer[i]){
                case (byte) ' ', (byte) '\t' -> {
                    if(argNumber == 0) pubArgs.setTopic(Arrays.copyOfRange(buffer, argStart, i));
                    else if(argNumber == 1) pubArgs.setSize(Integer.parseInt(new String(Arrays.copyOfRange(buffer, argStart, i))));
                    argStart = i + 1;
                    argNumber++;
                }
            }
        }
        if(argNumber == 1) pubArgs.setSize(Integer.parseInt(new String(Arrays.copyOfRange(buffer, argStart, buffer.length))));
        return pubArgs;
    }

    public static UnsubArgs getUnsubArgs(byte[] buffer){
        UnsubArgs unsubArgs = UnsubArgs.builder().build();
//        int argStart = 0;
//        int argNumber = 0;
        // TODO use these when extend unsub feature for number of messages
        for(int i=0;i<buffer.length;i++){
            switch (buffer[i]){
                case (byte) ' ', (byte) '\t', (byte) '\r'-> {
                    {unsubArgs.setSubId(new String(Arrays.copyOfRange(buffer, 0, i)));
                    }
                }
            }
            unsubArgs.setSubId(new String(Arrays.copyOfRange(buffer,0, buffer.length)));
        }
        return unsubArgs;
    }

    public static JsonObject getJsonObject(byte[] buffer){
        String userDetailsBytesToStrings
                = new String(buffer,
                StandardCharsets.UTF_8);
        JsonParser jsonParser = new JsonParser();
        return jsonParser.parse(userDetailsBytesToStrings).getAsJsonObject();
    }

}
