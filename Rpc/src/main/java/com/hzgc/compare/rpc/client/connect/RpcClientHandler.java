package com.hzgc.compare.rpc.client.connect;

import com.hzgc.compare.rpc.client.proxy.RPCFuture;
import com.hzgc.compare.rpc.protocol.JsonUtil;
import com.hzgc.compare.rpc.protocol.MsgType;
import com.hzgc.compare.rpc.protocol.RpcRequest;
import com.hzgc.compare.rpc.protocol.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);
    private ConcurrentHashMap<String, RPCFuture> pendingRPC = new ConcurrentHashMap<>();
    private volatile Channel channel;
    private InetSocketAddress remotePeer;
    private final AtomicInteger pingCount = new AtomicInteger(0);

    Channel getChannel() {
        return channel;
    }

    InetSocketAddress getRemotePeer() {
        return remotePeer;
    }

    /**
     * 连接成功后调用此方法
     *
     * @param ctx RpcClientHandler上下文
     * @throws Exception 可能抛出的异样
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = (InetSocketAddress) this.channel.remoteAddress();
    }

    /**
     * channel被注册后调用此方法
     *
     * @param ctx RpcClientHandler上下文
     * @throws Exception 可能抛出的异样
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    /**
     * 当有数据写入时调用此方法
     *
     * @param channelHandlerContext RpcClientHandler上下文
     * @param rpcResponse 请求消息对象
     * @throws Exception 可能抛出的异样
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        switch (rpcResponse.getType()) {
            case ASK:
                logger.debug("RpcResponse is " + JsonUtil.objectToJson(rpcResponse));
                String requestId = rpcResponse.getRequestId();
                RPCFuture rpcFuture = pendingRPC.get(requestId);
                rpcFuture.done(rpcResponse);
                break;
            case PONG:
                pingCount.set(0);
                if (pingCount.get() == 0) {
                    logger.debug("Check the server side connection successfull, reset pingCount");
                }
        }
    }

    /**
     * 当channel断开连接或者中断时会调用此方法
     * @param ctx RpcClientHandler上下文
     * @throws Exception 可能抛出的异样
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        logger.info("The Channel has an exception and may have been disconnected");
        removeCurrentHandler(ctx);
    }

    /**
     * 用来捕捉链路中可能产生的异常不让他向下一个链路抛
     *
     * @param ctx RpcClientHandler上下文
     * @param cause Throwable对象
     * @throws Exception 可能抛出的异样
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Client caugth exception", cause);
        ctx.close();
    }

    /**
     * 关闭channel
     */
    void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 向服务端发送请求
     *
     * @param rpcRequest rpc请求消息体
     * @return 异步返回Future调用对象
     */
    public RPCFuture sendRequest(RpcRequest rpcRequest) {
        //使用CountDownLatch来确认消息已发送
        final CountDownLatch latch = new CountDownLatch(1);
        RPCFuture rpcFuture = new RPCFuture(rpcRequest);
        //将异步请求结果添加至请求待完成集合中\
        pendingRPC.put(rpcRequest.getRequestId(), rpcFuture);
        //通过当前channel发送请求
        this.channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) channelFuture -> latch.countDown());
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
        return rpcFuture;
    }

    /**
     * 当发生读写超时的时候,即一定时间内没有调用读方法或写方法就会调用此方法
     *
     * @param ctx RpcClientHandler上下文
     * @param evt 触发事件
     * @throws Exception 可能抛出的异常
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (pingCount.get() <= 3) {
                RpcRequest request = new RpcRequest();
                request.setType(MsgType.PING);
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                channel.writeAndFlush(request).addListener((ChannelFutureListener) future -> {
                    pingCount.incrementAndGet();
                    countDownLatch.countDown();
                });
                countDownLatch.await();
                logger.debug("Try to check the server side connection, current attempts is:{}", pingCount.get());
            } else {
                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                    logger.info("Failed to get heartbeat from worker, worker ip is:{}, port is:{}, " +
                                    "more than {} retry attempts",
                            this.remotePeer.getHostString(),
                            this.remotePeer.getPort(),
                            pingCount.get());
                    removeCurrentHandler(ctx);
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 从ConnectManager中移除失效的RpcClientHandler
     * @param ctx RpcClientHandler上下文
     */
    private void removeCurrentHandler(ChannelHandlerContext ctx) {

        logger.info("Start closd current RpcClienthandler");
        ctx.channel().close();
        logger.info("Start remove current handler from ConnectManager");
        ConnectManager.getInstance().removeRpcClientHandler(this);
        ConnectManager.getInstance().removeConnectedServerNodes(this.getRemotePeer());
    }

    @Override
    public String toString() {
        return JsonUtil.objectToJson(this);
    }
}
