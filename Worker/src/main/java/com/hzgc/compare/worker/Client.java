package com.hzgc.compare.worker;

import com.hzgc.compare.api.Person;
import com.hzgc.compare.rpc.client.connect.RpcClient;
import com.hzgc.compare.rpc.client.connect.ServiceDiscovery;

public class Client {
    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient("172.18.18.105:2181");
        Person person = rpcClient.create(Person.class);
        person.say();
        System.out.println(person.give());
    }
}
