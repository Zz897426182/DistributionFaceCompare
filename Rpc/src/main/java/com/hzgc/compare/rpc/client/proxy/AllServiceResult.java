package com.hzgc.compare.rpc.client.proxy;

import java.util.List;

public interface AllServiceResult<T> {
    public List<T> getMore(T ret) ;
}
