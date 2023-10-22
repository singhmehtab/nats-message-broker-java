package org.project.service;

import org.apache.commons.lang3.ArrayUtils;
import org.project.Client;
import org.project.Subscription;
import org.project.Topic;
import org.project.dto.PubArgs;
import org.project.parse.ParseUtility;

import java.nio.ByteBuffer;

public class PublishService {

    private static final byte[] PUBLISH_START = "MSG ".getBytes();

    private static final byte[] PUBLISH_SPACE = " ".getBytes();

    private static final byte[] PUBLISH_CR_LF = "\r\n".getBytes();

    private static final byte[] PUBLISH_DOUBLE_CR_LF = "\r\n\r\n".getBytes();

    private static final byte[] PUBLISH_EMPTY = "0".getBytes();

    public static void produce(PubArgs pubArgs, Client client){
        Topic topic = SubscriptionService.getTopicHashMap().get(new String(pubArgs.getTopic()));
        if(topic == null){
            topic = new Topic(pubArgs.getTopic());
            SubscriptionService.getTopicHashMap().put(new String(pubArgs.getTopic()), topic);
        }
        if(client.isVerbose()) client.getResult().write(ParseUtility.OK_MESSAGE.clear());
        topic.getSubscriptionSet().parallelStream().forEach(subscription -> {
            subscription.getClient().getResult().write(ByteBuffer.wrap(getProducePayload(pubArgs, subscription)));
        });

    }

    private static byte[] getProducePayload(PubArgs pubArgs, Subscription subscription){
        byte[] prefix = ArrayUtils.addAll(PUBLISH_START, ArrayUtils.addAll(pubArgs.getTopic(), PUBLISH_SPACE));
        byte[] msg;
        if(pubArgs.getPayload() != null){
            msg = ArrayUtils.addAll(ArrayUtils.addAll(ArrayUtils.addAll(ByteBuffer.allocate(4).put(pubArgs.getSize().toString().getBytes()).array(), PUBLISH_CR_LF), pubArgs.getPayload()), PUBLISH_CR_LF);
        }
        else {
            msg = ArrayUtils.addAll(PUBLISH_EMPTY, PUBLISH_DOUBLE_CR_LF);
        }
        return ArrayUtils.addAll(ArrayUtils.addAll(prefix, (ArrayUtils.addAll(subscription.getSid(), " ".getBytes()))), msg);
    }

}
