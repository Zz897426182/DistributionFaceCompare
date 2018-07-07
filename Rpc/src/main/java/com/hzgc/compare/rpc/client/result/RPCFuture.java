package com.hzgc.compare.rpc.client.result;

import com.hzgc.compare.rpc.protocol.RpcRequest;
import com.hzgc.compare.rpc.protocol.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * RPCFuture实现了java concurrent包下的Future接口
 * Future接口主要用来处理异步请求结果
 * 在这里RPCFuture主要处理rpc进程间通信的请求结果
 */
public class RPCFuture implements Future<Object> {
    private static final Logger logger = LoggerFactory.getLogger(RPCFuture.class);
    //客户端向服务端发送的rpc请求消息体
    private RpcRequest rpcRequest;
    //服务端向客户端响应的rpc消息体
    private volatile RpcResponse rpcResponse;
    //请求起始时间
    private long startTime = System.currentTimeMillis();
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public RPCFuture(RpcRequest rpcRequest) {
        this.rpcRequest = rpcRequest;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    /**
     * 用来判断处理是否完成,此处只有在RpcResponse被赋值时表示完成处理
     *
     * @return 是否完成处理
     */
    @Override
    public boolean isDone() {
        return this.rpcResponse != null;
    }

    /**
     * 此方法用来获取处理结果,在未处理完之前通过countDownLatch.await()方法来进行阻塞
     * 直到完成为止,此方法有可能造成死锁
     *
     * @return 处理结果
     * @throws InterruptedException 可能抛出的异常
     * @throws ExecutionException   可能抛出的异常
     */
    @Override
    public Object get() throws InterruptedException, ExecutionException {
        if (this.rpcResponse != null) {
            return this.rpcResponse.getResult();
        } else {
            countDownLatch.await();
            if (this.rpcResponse != null) {
                return this.rpcResponse.getResult();
            } else {
                return null;
            }
        }
    }

    /**
     * 此方法用来获取处理结果,在未处理完之前通过countDownLatch.await(timeout, unit)方法来进行阻塞
     * 与get()方法不同的时这里的await方法会在指定的时间后会继续执行后续操作
     * 如果在指定时间之内被唤醒,那么将返回true,否则返回false
     *
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 处理结果
     * @throws InterruptedException 可能抛出的异常
     * @throws ExecutionException   可能抛出的异常
     * @throws TimeoutException     可能抛出的异常
     */
    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (this.rpcResponse != null) {
            return this.rpcResponse.getResult();
        } else {
            boolean success = countDownLatch.await(timeout, unit);
            if (success) {

                if (this.rpcResponse != null) {
                    return this.rpcResponse.getResult();
                } else {
                    return null;
                }
            } else {
                throw new RuntimeException("Timeout exception, request id is:" + this.rpcRequest.getRequestId() +
                        ", request class name is:" + this.rpcRequest.getClassName() +
                        ", request method is:" + this.rpcRequest.getMethodName());
            }
        }
    }

    /**
     * 此方法会在完成服务端响应时进行调用,并通过countDownLatch.countDown()方法进行唤醒
     *
     * @param rpcResponse 服务端向客户端响应的rpc消息体
     */

    public void done(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
        countDownLatch.countDown();
        long responseTime = System.currentTimeMillis() - this.startTime;
        // FIXME: 18-7-4
        long responseTimeThreshold = 5000;
        if (responseTime > responseTimeThreshold) {
            logger.warn("Service response time is too slow, request id is:{}, response time is:{}ms",
                    rpcResponse.getRequestId(), responseTime);
        } else {
            logger.debug("Service response time is {}", responseTime);
        }
    }
}


