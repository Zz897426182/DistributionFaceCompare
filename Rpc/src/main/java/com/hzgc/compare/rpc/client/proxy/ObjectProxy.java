package com.hzgc.compare.rpc.client.proxy;

import com.hzgc.compare.rpc.client.ConnectManager;
import com.hzgc.compare.rpc.client.netty.RpcClientHandler;
import com.hzgc.compare.rpc.client.result.RPCFuture;
import com.hzgc.compare.rpc.protocol.JsonUtil;
import com.hzgc.compare.rpc.protocol.MsgType;
import com.hzgc.compare.rpc.protocol.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class ObjectProxy<T> implements InvocationHandler, AsyncObjectProxy {
    private static final Logger logger = LoggerFactory.getLogger(ObjectProxy.class);
    private Class<T> clazz;

    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
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
        RpcClientHandler handler = ConnectManager.getInstance().chooseHandler();
        RPCFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture.get();
    }

    @Override
    public RPCFuture call(String funcName, Object... args) {
        RpcClientHandler handler = ConnectManager.getInstance().chooseHandler();
        RpcRequest request = createRequest(this.clazz.getName(), funcName, args);
        return handler.sendRequest(request);
    }

    private RpcRequest createRequest(String className, String methodName, Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setType(MsgType.ASK);
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameters(args);
        Class[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = getClassType(args[i]);
            request.setParameterTypes(parameterTypes);
        }
        logger.debug(className);
        logger.debug(methodName);
        for (Class parameterType : parameterTypes) {
            logger.debug(parameterType.getName());
        }
        for (Object arg : args) {
            logger.debug(arg.toString());
        }
        return request;
    }

    private Class getClassType(Object obj) {
        Class<?> classType = obj.getClass();
        String typeName = classType.getName();
        switch (typeName) {
            case "java.lang.Integer":
                return Integer.TYPE;
            case "java.lang.Long":
                return Long.TYPE;
            case "java.lang.Float":
                return Float.TYPE;
            case "java.lang.Double":
                return Double.TYPE;
            case "java.lang.Character":
                return Character.TYPE;
            case "java.lang.Boolean":
                return Boolean.TYPE;
            case "java.lang.Short":
                return Short.TYPE;
            case "java.lang.Byte":
                return Byte.TYPE;
        }
        return classType;
    }
}
