package com.hzgc.compare.rpc.client.proxy;

import com.hzgc.compare.rpc.client.ConnectManager;
import com.hzgc.compare.rpc.client.netty.RpcClientHandler;
import com.hzgc.compare.rpc.client.result.AllReturn;
import com.hzgc.compare.rpc.client.result.RPCFuture;
import com.hzgc.compare.rpc.protocol.JsonUtil;
import com.hzgc.compare.rpc.protocol.MsgType;
import com.hzgc.compare.rpc.protocol.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 用于返回所有服务端调用结果的代理对象
 *
 */
public class AllObjectProxy implements InvocationHandler, AsyncObjectProxy {
    private static final Logger logger = LoggerFactory.getLogger(ObjectProxy.class);

    @Override
    public RPCFuture call(String funcName, Object... args) {
        return null;
    }

    /**
     * 当调用方法的时候会先调用此代理方法
     *
     * @param proxy 代理对象
     * @param method 调用的方法对象
     * @param args 调用方法的参数
     * @return 调用返回的结果
     * @throws Throwable 可能抛出的异常
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String methodName = method.getName();
            if ("equals".equals(methodName)) {
                return proxy == args[0];
            } else if ("hashCode".equals(methodName)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(methodName)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", whith InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(methodName));
            }
        }
        RpcRequest request = new RpcRequest();
        request.setType(MsgType.ASK);
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        logger.debug("Invoke method is:" + JsonUtil.objectToJson(method));
        //从连接管理中拿出所有可用的连接
        CopyOnWriteArrayList<RpcClientHandler> handlersList = ConnectManager.getInstance().getConnectedHandlers();
        List<RPCFuture> rpcFutureList = new ArrayList<>();
        //向所有服务发送调用请求,此处不会阻塞,因为是异步接收结果
        for (RpcClientHandler handler : handlersList) {
            RPCFuture future = handler.sendRequest(request);
            rpcFutureList.add(future);
        }
        AllReturn receive = null;
        //获取结果并封装
        for (RPCFuture future : rpcFutureList) {
            AllReturn allReturn = (AllReturn) future.get();
            if (receive == null) {
                receive = allReturn;
            } else {
                receive.getResult().add(allReturn.getResult().get(0));
            }
        }
        return receive;
    }
}
