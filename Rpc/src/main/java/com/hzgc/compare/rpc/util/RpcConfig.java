package com.hzgc.compare.rpc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RpcConfig {
    private static final Logger logger = LoggerFactory.getLogger(RpcConfig.class);
    private static Properties properties;
    private static long curatorBaseSleepTime;
    private static long curatorMaxRetries;
    private static long zkSessionTime;
    private static int rpcServerThreadPoolSize;
    private static int rpcServerThreadPoolMaxSize;
    private static long rpcServerThreadPoolKeepAliveTime;
    private static int rpcServerThreadPollQueueSize;
    private static long rpcClientResponseTimeThreshold;
    private static long rpcConnectManagerConnectTimeout;
    static {
        InputStream stream = RpcConfig.class.getResourceAsStream("rpc.properties");
        properties = new Properties();
        try {
            if (stream != null) {
                properties.load(stream);
            } else {
                stream = RpcConfig.class.getResourceAsStream("META-INF/rpc.properties");
                properties.load(stream);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        setCuratorBaseSleepTime(Long.parseLong(properties.getProperty(ProperConstant.curatorBaseSleepTime)));
        setCuratorMaxRetries(Long.parseLong(properties.getProperty(ProperConstant.curatorMaxRetries)));
        setZkSessionTime(Long.parseLong(properties.getProperty(ProperConstant.zkSessionTime)));
    }

    public static long getCuratorBaseSleepTime() {
        return curatorBaseSleepTime;
    }

    public static void setCuratorBaseSleepTime(long curatorBaseSleepTime) {
        RpcConfig.curatorBaseSleepTime = curatorBaseSleepTime;
    }

    public static long getCuratorMaxRetries() {
        return curatorMaxRetries;
    }

    public static void setCuratorMaxRetries(long curatorMaxRetries) {
        RpcConfig.curatorMaxRetries = curatorMaxRetries;
    }

    public static long getZkSessionTime() {
        return zkSessionTime;
    }

    public static void setZkSessionTime(long zkSessionTime) {
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
