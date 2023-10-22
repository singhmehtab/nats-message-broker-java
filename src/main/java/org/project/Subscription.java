package org.project;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Subscription {

    private Client client;

    private byte[] sid;

}
