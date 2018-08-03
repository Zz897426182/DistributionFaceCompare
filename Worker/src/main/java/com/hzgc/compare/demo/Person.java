package com.hzgc.compare.demo;

import com.hzgc.compare.rpc.client.result.AllReturn;

public interface Person {
    public AllReturn<String> giveMore();

    public AllReturn<Five> getFive() throws InterruptedException;

}
