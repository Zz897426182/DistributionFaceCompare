package com.hzgc.compare.rpc.client.proxy;

import java.util.ArrayList;
import java.util.List;

/**
 * 如果需要返回多服务端调用结果,需要使用此对象作为接口返回的封装
 * 比如有一个Perosn这个对象作为接口返回,那么在定义接口的时候需要改写为 public AllReturn<Person> methodName(...)
 *
 * @param <T>
 */
public class AllReturn<T> {
    List<T> result;

    public AllReturn(T ret) {
        result = new ArrayList<>();
        result.add(ret);
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }
}
