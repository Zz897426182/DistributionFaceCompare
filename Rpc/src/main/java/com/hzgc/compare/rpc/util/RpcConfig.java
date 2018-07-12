package com.hzgc.compare.rpc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RpcConfig {
    private static final Logger logger = LoggerFactory.getLogger(RpcConfig.class);
    private static Properties properties;
    private static int curatorBaseSleepTime;
    private static int curatorMaxRetries;
    private static int zkSessionTime;
    private static int rpcServerThreadPoolSize;
    private static int rpcServerThreadPoolMaxSize;
    private static long rpcServerThreadPoolKeepAliveTime;
    private static int rpcServerThreadPollQueueSize;
    private static long rpcClientResponseTimeThreshold;
    private static long rpcConnectManagerConnectTimeout;
    static {
        InputStream stream = RpcConfig.class.getClassLoader().getResourceAsStream("rpc.properties");
        properties = new Properties();
        try {
            if (stream != null) {
                logger.info("Find rpc.properties in classpath, load it");
                properties.load(stream);
            } else {
                logger.info("Not find rpc.properties in classpath, load default file from META-INF/rpc.propertites");
                stream = RpcConfig.class.getClassLoader().getResourceAsStream("META-INF/rpc.properties");
                properties.load(stream);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        setCuratorBaseSleepTime(Integer.parseInt(properties.getProperty(ProperConstant.curatorBaseSleepTime)));
        setCuratorMaxRetries(Integer.parseInt(properties.getProperty(ProperConstant.curatorMaxRetries)));
        setZkSessionTime(Integer.parseInt(properties.getProperty(ProperConstant.zkSessionTime)));
        setRpcServerThreadPoolSize(Integer.parseInt(properties.getProperty(ProperConstant.rpcServerThreadPoolSize)));
        setRpcServerThreadPoolMaxSize(Integer.parseInt(properties.getProperty(ProperConstant.rpcServerThreadPoolMaxSize)));
        setRpcServerThreadPoolKeepAliveTime(Long.parseLong(properties.getProperty(ProperConstant.rpcServerThreadPoolKeepAliveTime)));
        setRpcServerThreadPollQueueSize(Integer.parseInt(properties.getProperty(ProperConstant.rpcServerThreadPollQueueSize)));
        setRpcClientResponseTimeThreshold(Long.parseLong(properties.getProperty(ProperConstant.rpcClientResponseTimeThreshold)));
        setRpcConnectManagerConnectTimeout(Long.parseLong(properties.getProperty(ProperConstant.rpcConnectManagerConnectTimeout)));
    }

    public static int getCuratorBaseSleepTime() {
        return curatorBaseSleepTime;
    }

    public static void setCuratorBaseSleepTime(int curatorBaseSleepTime) {
        RpcConfig.curatorBaseSleepTime = curatorBaseSleepTime;
    }

    public static int getCuratorMaxRetries() {
        return curatorMaxRetries;
    }

    public static void setCuratorMaxRetries(int curatorMaxRetries) {
        RpcConfig.curatorMaxRetries = curatorMaxRetries;
    }

    public static int getZkSessionTime() {
        return zkSessionTime;
    }

    public static void setZkSessionTime(int zkSessionTime) {
        RpcConfig.zkSessionTime = zkSessionTime;
    }

    public static int getRpcServerThreadPoolSize() {
        return rpcServerThreadPoolSize;
    }

    public static void setRpcServerThreadPoolSize(int rpcServerThreadPoolSize) {
        RpcConfig.rpcServerThreadPoolSize = rpcServerThreadPoolSize;
    }

    public static int getRpcServerThreadPoolMaxSize() {
        return rpcServerThreadPoolMaxSize;
    }

    public static void setRpcServerThreadPoolMaxSize(int rpcServerThreadPoolMaxSize) {
        RpcConfig.rpcServerThreadPoolMaxSize = rpcServerThreadPoolMaxSize;
    }

    public static long getRpcServerThreadPoolKeepAliveTime() {
        return rpcServerThreadPoolKeepAliveTime;
    }

    public static void setRpcServerThreadPoolKeepAliveTime(long rpcServerThreadPoolKeepAliveTime) {
        RpcConfig.rpcServerThreadPoolKeepAliveTime = rpcServerThreadPoolKeepAliveTime;
    }

    public static int getRpcServerThreadPollQueueSize() {
        return rpcServerThreadPollQueueSize;
    }

    public static void setRpcServerThreadPollQueueSize(int rpcServerThreadPollQueueSize) {
        RpcConfig.rpcServerThreadPollQueueSize = rpcServerThreadPollQueueSize;
    }

    public static long getRpcClientResponseTimeThreshold() {
        return rpcClientResponseTimeThreshold;
    }

    public static void setRpcClientResponseTimeThreshold(long rpcClientResponseTimeThreshold) {
        RpcConfig.rpcClientResponseTimeThreshold = rpcClientResponseTimeThreshold;
    }

    public static long getRpcConnectManagerConnectTimeout() {
        return rpcConnectManagerConnectTimeout;
    }

    public static void setRpcConnectManagerConnectTimeout(long rpcConnectManagerConnectTimeout) {
        RpcConfig.rpcConnectManagerConnectTimeout = rpcConnectManagerConnectTimeout;
    }
}
