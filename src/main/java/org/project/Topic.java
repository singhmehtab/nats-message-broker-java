package org.project;

import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Topic {

    private final byte[] name;

    @Getter
    private Set<Subscription> subscriptionSet;

    public Topic(byte[] name) {
        this.name = name;
        subscriptionSet = Collections.synchronizedSet(new HashSet<>());
    }

    public synchronized void sub(Subscription subscription){
        subscriptionSet.add(subscription);
    }

}
