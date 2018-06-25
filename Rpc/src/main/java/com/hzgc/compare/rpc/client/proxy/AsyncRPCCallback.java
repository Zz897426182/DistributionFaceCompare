package com.hzgc.compare.rpc.client.proxy;

public interface AsyncRPCCallback {
    void success(Object result);
    void fail(Exception e);
}
