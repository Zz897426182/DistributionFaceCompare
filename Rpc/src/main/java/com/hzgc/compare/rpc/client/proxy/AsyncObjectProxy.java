package com.hzgc.compare.rpc.client.proxy;

public interface AsyncObjectProxy {
    public RPCFuture call(String funcName, Object... args);
}