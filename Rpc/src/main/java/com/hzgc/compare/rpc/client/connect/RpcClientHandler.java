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
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);
    private ConcurrentHashMap<String, RPCFuture> pendingRPC = new ConcurrentHashMap<>();
    private volatile Channel channel;
    private SocketAddress remotePeer;
    private final AtomicInteger pingCount = new AtomicInteger(0);

    public Channel getChannel() {
        return channel;
    }

    public SocketAddress getRemotePeer() {
        return remotePeer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

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
                    logger.info("Check the server side connection successfull, reset pingCount");
                }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        removeCurrentHandler(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Client caugth exception", cause);
        ctx.close();
    }

    void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public RPCFuture sendRequest(RpcRequest rpcRequest) {
        final CountDownLatch latch = new CountDownLatch(1);
        RPCFuture rpcFuture = new RPCFuture(rpcRequest);
        pendingRPC.put(rpcRequest.getRequestId(), rpcFuture);
        channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) channelFuture -> latch.countDown());
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
        return rpcFuture;
    }

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
                logger.info("Try to check the server side connection, current attempts is:{}", pingCount.get());
            } else {
                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                    removeCurrentHandler(ctx);
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void removeCurrentHandler(ChannelHandlerContext ctx) {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        logger.info("Failed to get heartbeat from worker, worker ip is:{}, port is:{}",
                socketAddress.getHostString(),
                socketAddress.getPort());
        logger.info("Start closd current RpcClienthandler");
        ctx.channel().close();
        logger.info("Start remove current handler from ConnectManager");
        ConnectManager.getInstance().removeRpcClientHandler(this);
        ConnectManager.getInstance().removeConnectedServerNodes((InetSocketAddress) this.getRemotePeer());
    }

    @Override
    public String toString() {
        return JsonUtil.objectToJson(this);
    }
}
