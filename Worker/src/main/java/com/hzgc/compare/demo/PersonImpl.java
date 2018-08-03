package com.hzgc.compare.demo;

import com.hzgc.compare.rpc.client.result.AllReturn;
import com.hzgc.compare.rpc.server.annotation.RpcService;

@RpcService(Person.class)
public class PersonImpl implements Person{

    @Override
    public AllReturn<String> giveMore() {
        return new AllReturn<>("1234");
    }

    @Override
    public AllReturn<Five> getFive() throws InterruptedException {
        Thread.sleep(1000L * 10);
        Five five = new Five();
        return new AllReturn<>(five);
    }
}
