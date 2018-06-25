package com.hzgc.compare.rpc.client.proxy;

import com.google.common.collect.Lists;
import com.hzgc.compare.rpc.client.RpcClient;
import com.hzgc.compare.rpc.protocol.RpcRequest;
import com.hzgc.compare.rpc.protocol.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

public class RPCFuture implements Future<Object> {
    private static final Logger logger = LoggerFactory.getLogger(RPCFuture.class);
    private Sync sync;
    private RpcRequest rpcRequest;
    private RpcResponse rpcResponse;
    private long startTIme;
    private List<AsyncRPCCallback> pendingCallbacks = Lists.newArrayList();
    private ReentrantLock lock = new ReentrantLock();

    public RPCFuture(RpcRequest rpcRequest) {
        this.sync = new Sync();
        this.rpcRequest = rpcRequest;
        this.startTIme = System.currentTimeMillis();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);
        if (this.rpcResponse != null) {
            return this.rpcResponse.getResult();
        } else {
            return null;
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
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

    public void done(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
        sync.release(1);
        invokeCallbacks();
        long responseTime = System.currentTimeMillis() - this.startTIme;
        long responseTimeThreshold = 5000;
        if (responseTime > responseTimeThreshold) {
            logger.warn("Service response time is too slow, request id is:{}, response time is:{}ms",
                    rpcResponse.getRequestId(), responseTime);
        }
    }

    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRPCCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    public RPCFuture addCallback(AsyncRPCCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void runCallback(AsyncRPCCallback callback) {
        final RpcResponse response = this.rpcResponse;
        RpcClient.submit(() -> {
            if (!response.isError()) {
                callback.success(response.getResult());
            } else {
                callback.fail(new RuntimeException("Response error", new Throwable(response.getError())));
            }
        });
    }

    static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1L;
        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() != pending || compareAndSetState(pending, done);
        }

        boolean isDone() {
            int state = getState();
            return state == done;
        }
    }
}


