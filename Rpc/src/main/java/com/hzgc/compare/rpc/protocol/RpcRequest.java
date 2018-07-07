package com.hzgc.compare.rpc.protocol;

/**
 * rpc请求消息对象，封装了请求信息
 */
public class RpcRequest extends BaseMsg {
    //请求ID，用UUID生成的
    private String requestId;
    //要调用了类名称，ex:com.hzgc.compare.ClassName
    private String className;
    //要调用的方法名称
    private String methodName;
    //参数的对象类集合
    private Class<?>[] parameterTypes;
    //请求参数集合
    private Object[] parameters;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
