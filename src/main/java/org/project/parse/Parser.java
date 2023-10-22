package org.project.parse;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.ArrayUtils;
import org.project.*;
import org.project.dto.PubArgs;
import org.project.dto.UnsubArgs;
import org.project.service.PublishService;
import org.project.service.SubscriptionService;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;

public class Parser {

    // Using global variables to reduce garbage collection even thought some of these are being used only in parse method only.

    private STATE state = STATE.OP_START;

    private int argStart = 0;

    private byte[] argBuffer;

    private int drop = 0;

    private Client client;

    private PubArgs pubArgs;

    private JsonObject connectArgs;

    public Parser(Client client){
        this.client = client;
    }

    enum STATE{
        OP_START,
        OP_C,
        OP_CO,
        OP_CON,
        OP_CONN,
        OP_CONNE,
        OP_CONNEC,
        OP_CONNECT,
        OP_CONNECT_CR,
        OP_CONNECT_LF,
        OP_CONNECT_OB,
        OP_CONNECT_CB,
        OP_CONNECT_ARGS,
        OP_CONNECT_SPACE,
        OP_P,
        OP_PI,
        OP_PIN,
        OP_PING,
        OP_PING_CR,
        OP_PING_LF,
        OP_S,
        OP_SU,
        OP_SUB,
        OP_U,
        OP_UN,
        OP_UNS,
        OP_UNSU,
        OP_UNSUB,
        OP_UNSUB_ARGS,
        OP_SUB_SPACE,
        OP_SUB_ARG,
        OP_PU,
        OP_PUB,
        OP_PUB_ARG,
        OP_MSG_PAYLOAD,
        OP_MSG_PAYLOAD_EXPECT_CR,
        OP_MSG_PAYLOAD_CR,
        INVALID;
    }

    enum BufChar {
        CHAR_C((byte) 'C'),
        CHAR_c((byte) 'c'),
        CHAR_O((byte) 'O'),
        CHAR_o((byte) 'o'),
        CHAR_N((byte) 'N'),
        CHAR_n((byte) 'n'),
        CHAR_E((byte) 'E'),
        CHAR_e((byte) 'e'),
        CHAR_T((byte) 'T'),
        CHAR_t((byte) 't'),

        CHAR_CR((byte) '\r'),

        CHAR_LF((byte) '\n'),
        CHAR_OB((byte) '{'),
        CHAR_CB((byte) '}'),
        CHAR_SPACE((byte) ' '),
        CHAR_P((byte) 'P'),
        CHAR_p((byte) 'p'),
        CHAR_i((byte) 'i'),
        CHAR_I((byte) 'I'),
        CHAR_G((byte) 'G'),
        CHAR_g((byte) 'g'),
        CHAR_S((byte) 'S'),
        CHAR_s((byte) 's'),
        CHAR_u((byte) 'u'),
        CHAR_U((byte)'U'),
        CHAR_B((byte)'B'),
        CHAR_b((byte) 'b'),
        CHAR_TAB((byte) '\t');

        public final byte byteValue;

        BufChar(byte c) {
            this.byteValue = c;
        }

        public byte getByteValue() {
            return byteValue;
        }
    }

    public void parse(byte[] buffer, AsynchronousSocketChannel result){
        this.argStart = 0;
        this.drop = 0;
        int i;
        for(i=0;i<buffer.length;i++){
         switch (state){
             case OP_START -> {
               if(buffer[i] == BufChar.CHAR_C.getByteValue() || buffer[i] == BufChar.CHAR_c.getByteValue()){
                   state = STATE.OP_C;
               }
               else if(buffer[i] == BufChar.CHAR_P.getByteValue() || buffer[i] == BufChar.CHAR_p.getByteValue()){
                   state = STATE.OP_P;
               }
               else if(buffer[i] == BufChar.CHAR_S.getByteValue() || buffer[i] == BufChar.CHAR_s.getByteValue()) state = STATE.OP_S;
               else if(buffer[i] == BufChar.CHAR_U.getByteValue() || buffer[i] == BufChar.CHAR_u.getByteValue()) state = STATE.OP_U;
               else{
                   state = STATE.INVALID;
               }
             }
             case OP_C -> {
                 if(buffer[i] == BufChar.CHAR_O.getByteValue() || buffer[i] == BufChar.CHAR_o.getByteValue()){
                     state = STATE.OP_CO;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_CO, OP_CON -> {
                 if(buffer[i] == BufChar.CHAR_N.getByteValue() || buffer[i] == BufChar.CHAR_n.getByteValue()){
                     if(state == STATE.OP_CO) {
                         state = STATE.OP_CON;
                     }
                     else{
                         state = STATE.OP_CONN;
                     }
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_CONN -> {
                 if(buffer[i] == BufChar.CHAR_E.getByteValue() || buffer[i] == BufChar.CHAR_e.getByteValue()){
                     state = STATE.OP_CONNE;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_CONNE -> {
                 if(buffer[i] == BufChar.CHAR_c.getByteValue() || buffer[i] == BufChar.CHAR_C.getByteValue()){
                     state = STATE.OP_CONNEC;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_CONNEC -> {
                 if(buffer[i] == BufChar.CHAR_t.getByteValue() || buffer[i] == BufChar.CHAR_T.getByteValue()){
                     state = STATE.OP_CONNECT;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_CONNECT -> {
                 if(buffer[i] == BufChar.CHAR_CR.getByteValue()) state = STATE.OP_CONNECT_CR;
                 else if(buffer[i] == BufChar.CHAR_SPACE.getByteValue()) state = STATE.OP_CONNECT_SPACE;
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_CONNECT_CR -> {
                 if(buffer[i] == BufChar.CHAR_LF.getByteValue()) state = STATE.OP_CONNECT_LF;
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_CONNECT_OB -> {
                 if(buffer[i] == BufChar.CHAR_CB.getByteValue()) state = STATE.OP_CONNECT_CB;
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_CONNECT_SPACE -> {
                 if(buffer[i] == BufChar.CHAR_SPACE.getByteValue()) continue;
                else {
                    state = STATE.OP_CONNECT_ARGS;
                    this.argStart = i;
                 }
             }
             case OP_CONNECT_ARGS -> {
                 if(buffer[i] == BufChar.CHAR_CR.getByteValue()){
                     this.drop = 1;
                 }
                 else if(buffer[i] == BufChar.CHAR_LF.getByteValue()){
                     byte[] arg;
                     if(this.argBuffer == null) {
                         arg = Arrays.copyOfRange(buffer, this.argStart, i - this.drop);
                     }
                     else {
                         arg = ArrayUtils.addAll(this.argBuffer, Arrays.copyOfRange(buffer, this.argStart, i - this.drop));
                     }
                     this.argStart =  i + 1;
                     this.drop = 0;
                     state = STATE.OP_START;
                     this.connectArgs = ParseUtility.getJsonObject(arg);
                     if(this.connectArgs.has("verbose"))this.client.setVerbose(this.connectArgs.get("verbose").getAsBoolean());
                     this.argBuffer = null;
                     if(client.isVerbose())client.getResult().write(ParseUtility.OK_MESSAGE.clear());
                 }
             }
             case OP_P -> {
                 if(buffer[i] == BufChar.CHAR_I.getByteValue() || buffer[i] == BufChar.CHAR_i.getByteValue()){
                     state = STATE.OP_PI;
                 }
                 else if(buffer[i] == BufChar.CHAR_U.getByteValue() || buffer[i] == BufChar.CHAR_u.getByteValue()){
                     state = STATE.OP_PU;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_PI -> {
                 if(buffer[i] == BufChar.CHAR_N.getByteValue() || buffer[i] == BufChar.CHAR_n.getByteValue()){
                     state = STATE.OP_PIN;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_PIN -> {
                 if(buffer[i] == BufChar.CHAR_G.getByteValue() || buffer[i] == BufChar.CHAR_g.getByteValue()){
                     state = STATE.OP_PING;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_PING -> {
                 if(buffer[i] == BufChar.CHAR_CR.getByteValue()){
                     state = STATE.OP_PING_CR;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_PING_CR -> {
                 if(buffer[i] == BufChar.CHAR_LF.getByteValue()){
                     state = STATE.OP_PING_LF;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_S -> {
                 if(buffer[i] == BufChar.CHAR_U.getByteValue() || buffer[i] == BufChar.CHAR_u.getByteValue()){
                     state = STATE.OP_SU;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_SU -> {
                 if(buffer[i] == BufChar.CHAR_B.getByteValue() || buffer[i] == BufChar.CHAR_b.getByteValue()){
                     state = STATE.OP_SUB;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_SUB -> {
                 if(buffer[i] == BufChar.CHAR_SPACE.getByteValue() || buffer[i] == BufChar.CHAR_TAB.getByteValue()) state = STATE.OP_SUB_SPACE;
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_SUB_SPACE -> {
                 if(buffer[i] == BufChar.CHAR_SPACE.getByteValue() || buffer[i] == BufChar.CHAR_TAB.getByteValue()){
                     continue;
                 }
                 else{
                     state = STATE.OP_SUB_ARG;
                     this.argStart = i;
                 }
             }
             case OP_SUB_ARG -> {
                 if(buffer[i] == BufChar.CHAR_CR.getByteValue()){
                     this.drop = 1;
                 }
                 else if(buffer[i] == BufChar.CHAR_LF.getByteValue()){
                     byte[] arg;
                     if(this.argBuffer == null) {
                         arg = Arrays.copyOfRange(buffer, this.argStart, i - this.drop);
                     }
                     else {
                         arg = ArrayUtils.addAll(this.argBuffer, Arrays.copyOfRange(buffer, this.argStart, i - this.drop));
                     }
                     SubscriptionService.subscribe(client, arg);
                     this.argBuffer = null;
                     this.argStart = i+1;
                     this.drop = 0;
                     state = STATE.OP_START;
                 }
             }
             case OP_U -> {
                 if(buffer[i] == BufChar.CHAR_N.getByteValue() || buffer[i] == BufChar.CHAR_n.getByteValue()){
                     state = STATE.OP_UN;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_UN -> {
                 if(buffer[i] == BufChar.CHAR_S.getByteValue() || buffer[i] == BufChar.CHAR_s.getByteValue()){
                     state = STATE.OP_UNS;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_UNS -> {
                 if(buffer[i] == BufChar.CHAR_U.getByteValue() || buffer[i] == BufChar.CHAR_u.getByteValue()){
                     state = STATE.OP_UNSU;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_UNSU -> {
                 if(buffer[i] == BufChar.CHAR_B.getByteValue() || buffer[i] == BufChar.CHAR_b.getByteValue()){
                     state = STATE.OP_UNSUB;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_UNSUB -> {
                 if(buffer[i] == BufChar.CHAR_SPACE.getByteValue() || buffer[i] == BufChar.CHAR_TAB.getByteValue()){
                     continue;
                 }
                 else{
                     state = STATE.OP_UNSUB_ARGS;
                     this.argStart = i;
                 }
             }
             case OP_UNSUB_ARGS -> {
                 if(buffer[i] == BufChar.CHAR_CR.getByteValue()){
                     this.drop = 1;
                 }
                 else if(buffer[i] == BufChar.CHAR_LF.getByteValue()){
                     byte[] arg;
                     if(this.argBuffer == null) {
                         arg = Arrays.copyOfRange(buffer, this.argStart, i - this.drop);
                     }
                     else {
                         arg = ArrayUtils.addAll(this.argBuffer, Arrays.copyOfRange(buffer, this.argStart, i - this.drop));
                     }
                     this.argStart =  i + 1;
                     this.drop = 0;
                     state = STATE.OP_START;
                     UnsubArgs unsubArgs = ParseUtility.getUnsubArgs(arg);
                     SubscriptionService.unsubscribe(client, unsubArgs.getSubId().getBytes());
                     this.argBuffer = null;
                 }
             }
             case OP_PU -> {
                 if(buffer[i] == BufChar.CHAR_B.getByteValue() || buffer[i] == BufChar.CHAR_b.getByteValue()){
                     state = STATE.OP_PUB;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
             case OP_PUB -> {
                 if(buffer[i] == BufChar.CHAR_SPACE.getByteValue()){
                     continue;
                 }
                 else{
                     state = STATE.OP_PUB_ARG;
                     this.argStart = i;
                 }
             }
             case OP_PUB_ARG -> {
                 if(buffer[i] == BufChar.CHAR_CR.getByteValue()){
                     this.drop = 1;
                 }
                 else if(buffer[i] == BufChar.CHAR_LF.getByteValue()){
                     byte[] arg;
                     if(this.argBuffer == null) {
                         arg = Arrays.copyOfRange(buffer, this.argStart, i - this.drop);
                     }
                     else {
                         arg = ArrayUtils.addAll(this.argBuffer, Arrays.copyOfRange(buffer, this.argStart, i - this.drop));
                     }
                     this.argStart =  i + 1;
                     this.drop = 0;
                     state = STATE.OP_MSG_PAYLOAD;
                     this.pubArgs = ParseUtility.getPubArgs(arg);
                     this.argBuffer = null;
                 }
             }
             case OP_MSG_PAYLOAD -> {
                 if(this.argBuffer != null){
                     if(i + this.argBuffer.length + 1 >= (this.pubArgs.getSize())){
                         this.state = STATE.OP_MSG_PAYLOAD_EXPECT_CR;
                     }
                     else continue;
                 }
                 else if(i - this.argStart + 1 >= this.pubArgs.getSize()){
                        this.state = STATE.OP_MSG_PAYLOAD_EXPECT_CR;
                 }
             }
             case OP_MSG_PAYLOAD_EXPECT_CR -> {
                 if(buffer[i] == BufChar.CHAR_CR.getByteValue()){
                     state = STATE.OP_MSG_PAYLOAD_CR;
                     this.drop = 1;
                 }
             }
             case OP_MSG_PAYLOAD_CR -> {
                 if(buffer[i] == BufChar.CHAR_LF.getByteValue()){
                    if(this.argBuffer != null){
                        this.pubArgs.setPayload(ArrayUtils.addAll(this.argBuffer, Arrays.copyOfRange(buffer, this.argStart, i - this.drop)));
                    }
                    else {
                        this.pubArgs.setPayload(Arrays.copyOfRange(buffer, this.argStart, i - this.drop));
                    }
                    PublishService.produce(this.pubArgs, client);
                    this.argBuffer = null;
                    this.pubArgs = null;
                    this.argStart = i + 1;
                    this.drop = 0;
                    this.state = STATE.OP_START;
                 }
                 else{
                     state = STATE.INVALID;
                 }
             }
         }
         if(state == STATE.INVALID){
             result.write(ParseUtility.INVALID_MESSAGE.clear());
             state = STATE.OP_START;
             break;
         }
        }
        if(state == STATE.OP_SUB_ARG || state == STATE.OP_PUB_ARG || state == STATE.OP_CONNECT_ARGS || state == STATE.OP_UNSUB_ARGS){
            if(this.argBuffer == null){
                this.argBuffer = Arrays.copyOfRange(buffer, this.argStart, i - this.drop);
            }
            else{
                this.argBuffer = ArrayUtils.addAll(this.argBuffer, Arrays.copyOfRange(buffer, this.argStart, i - this.drop));
            }
        }
        else if(state == STATE.OP_MSG_PAYLOAD || state == STATE.OP_MSG_PAYLOAD_EXPECT_CR){
            if(this.argBuffer == null){
                this.argBuffer = Arrays.copyOfRange(buffer, this.argStart, i - this.drop);
            }
            else{
                this.argBuffer = ArrayUtils.addAll(this.argBuffer, Arrays.copyOfRange(buffer, this.argStart, i - this.drop));
            }
        }
        else if(state == STATE.OP_CONNECT_LF || state == STATE.OP_CONNECT_CB){
            if(client.isVerbose())result.write(ParseUtility.OK_MESSAGE.clear());
            state = STATE.OP_START;
        }
        else if(state == STATE.OP_PING_LF){
            result.write(ParseUtility.PONG_MESSAGE.clear());
            state = STATE.OP_START;
        }
    }

}
