package com.hzgc.compare.rpc.client.proxy;

import com.hzgc.compare.rpc.client.result.RPCFuture;

public interface AsyncObjectProxy {
    RPCFuture call(String funcName, Object... args);
}