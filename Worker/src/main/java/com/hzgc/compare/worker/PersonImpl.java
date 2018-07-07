package com.hzgc.compare.worker;

import com.hzgc.compare.rpc.client.result.AllReturn;
import com.hzgc.compare.rpc.server.annotation.RpcService;

@RpcService(Person.class)
public class PersonImpl implements Person{

    @Override
    public AllReturn<String> giveMore() {
        return new AllReturn<>("1234");
    }

    @Override
    public AllReturn<Five> getFive() {

        Five five = new Five();
        return new AllReturn<>(five);
    }
}
