package org.project.service;

import lombok.Getter;
import org.project.Client;
import org.project.Subscription;
import org.project.Topic;
import org.project.dto.SubArgs;
import org.project.parse.ParseUtility;

import java.util.*;

public class SubscriptionService {

    @Getter
    private static final Map<String, Topic> topicHashMap = new HashMap<>();

    private static final Map<Client, Map<String, List<Topic>>> subIdTopicHashMap = new HashMap<>();

    public static void subscribe(Client client, byte[] buffer){
        SubArgs subArgs = ParseUtility.getSubArgs(buffer);
        Subscription subscription = new Subscription(client, subArgs.getSubId());
        String topicName = new String(subArgs.getTopic());
        String subId = new String(subArgs.getSubId());
        if(topicHashMap.containsKey(topicName)){
            topicHashMap.get(topicName).sub(subscription);
        }
        else {
            Topic topic = new Topic(subArgs.getTopic());
            topic.sub(subscription);
            synchronized (SubscriptionService.class) {
                topicHashMap.put(new String(subArgs.getTopic()), topic);
            }
        }
        addSubscription(client, subId, topicName);
        if(client.isVerbose())client.getResult().write(ParseUtility.OK_MESSAGE.clear());
    }

    public static void unsubscribe(Client client, byte[] subscriptionId){
        try {
            List<Topic> topicArrayList = subIdTopicHashMap.get(client).get(new String(subscriptionId));
            topicArrayList.parallelStream().forEach( (topic)-> {
                Iterator<Subscription> iterator = topic.getSubscriptionSet().iterator();
                synchronized (iterator){
                    Subscription s;
                    while (iterator.hasNext()) {
                        s = iterator.next();
                        if(Arrays.equals(s.getSid(), subscriptionId) && s.getClient().equals(client)){
                            iterator.remove();
                        }
                    }
                }
            });
            if(client.isVerbose())client.getResult().write(ParseUtility.OK_MESSAGE.clear());
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private synchronized static void addSubscription(Client client, String subId, String topicName){
        subIdTopicHashMap.put(client, subIdTopicHashMap.getOrDefault(client, new HashMap<>()));
        List<Topic> topicList = subIdTopicHashMap.get(client).getOrDefault(subId, new ArrayList<>());
        topicList.add(topicHashMap.get(topicName));
        subIdTopicHashMap.get(client).put(subId, topicList);
    }
}
