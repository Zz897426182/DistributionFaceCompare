package com.hzgc.compare.demo;

import com.hzgc.compare.rpc.client.RpcClient;
import com.hzgc.compare.rpc.client.result.AllReturn;

public class Client {
    public static void main(String[] args) {
        RpcClient rpcClient = new RpcClient("172.18.18.105:2181");
        Person person = rpcClient.createAll(Person.class);
        long star = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
//            person.giveMore();
            AllReturn<Five> fiveAllReturn = person.getFive();
//            System.out.println(JsonUtil.objectToJson(person.getFive()));
        }
        System.out.println("++++++++++++++++++++++++++++++++++");
        System.out.println(System.currentTimeMillis() - star);
    }
}
