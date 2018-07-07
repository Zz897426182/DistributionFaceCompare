package com.hzgc.compare.rpc.client;

import com.hzgc.compare.rpc.client.zk.ServiceDiscovery;
import com.hzgc.compare.rpc.client.proxy.AllObjectProxy;
import com.hzgc.compare.rpc.client.proxy.AsyncObjectProxy;
import com.hzgc.compare.rpc.client.proxy.ObjectProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);
    private ServiceDiscovery serviceDiscovery;
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16,
            16,
            600L,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(65535));

    public RpcClient(String serverAddress) {
        this.serviceDiscovery = new ServiceDiscovery(serverAddress);
    }

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * 此处使用了Java动态代理技术
     * 通过代理来屏蔽远程调用的操作并返回一个与入参interfaceClass相同类型的代理对象
     *
     * @param interfaceClass 要被代理的接口类
     * @param <T>            接口类的类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> interfaceClass) {
        logger.info("Create this class proxy object, class name is:", interfaceClass.getName());
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ObjectProxy<>(interfaceClass));
    }

    /**
     * 此处使用了Java动态代理技术
     * 通过代理来屏蔽远程调用的操作并返回一个与入参interfaceClass相同类型的代理对象
     *
     * @param interfaceClass 要被代理的接口类
     * @param <T>            接口类的类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T createAll(Class<T> interfaceClass) {
        logger.info("Create this class proxy object, class name is:", interfaceClass.getName());
        logger.info("The current proxy object will get the data set of all service nodes");
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new AllObjectProxy());
    }

    public <T> AsyncObjectProxy createAsync(Class<T> interfaceClass) {
        logger.info("Create this class proxy object by async, class name is:", interfaceClass.getName());
        return new ObjectProxy<>(interfaceClass);
    }

    public static void submit(Runnable task) {
        logger.debug("Submit task " + task.toString());
        threadPoolExecutor.execute(task);
    }

    public void stop() {
        threadPoolExecutor.shutdown();
        ;
        serviceDiscovery.stop();
        ConnectManager.getInstance().stop();
    }
}
