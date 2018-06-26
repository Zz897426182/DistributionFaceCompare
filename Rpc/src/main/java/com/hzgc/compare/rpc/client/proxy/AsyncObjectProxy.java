package com.hzgc.compare.rpc.client.proxy;

public interface AsyncObjectProxy {
    RPCFuture call(String funcName, Object... args);
}