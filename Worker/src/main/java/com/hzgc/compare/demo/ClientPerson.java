package com.hzgc.compare.demo;

import com.hzgc.compare.rpc.client.RpcClient;
import com.hzgc.compare.rpc.client.result.AllReturn;
import com.hzgc.compare.worker.Service;

public class ClientPerson {
    public static void main(String args[]) throws InterruptedException {
        RpcClient rpcClient = new RpcClient("172.18.18.105:2181,172.18.18.106:2181,172.18.18.107:2181");
        Thread.sleep(3000);
//        Person person = rpcClient.createAll(Person.class);
//        AllReturn<Five> res = person.getFive();
//        System.out.println(res.getResult());

        Service service = rpcClient.createAll(Service.class);
        AllReturn<String> resu = service.test();
        System.out.println(resu.getResult());
    }
}
