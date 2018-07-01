package com.hzgc.compare.worker;

import com.hzgc.compare.api.Person;
import com.hzgc.compare.rpc.server.annotation.RpcService;

@RpcService(Person.class)
public class PersonImpl implements Person {
    public void say() {
        System.out.println(Thread.currentThread().getName() + ": say hello");
    }

    public String give() {
        return "give me five";
    }
}
