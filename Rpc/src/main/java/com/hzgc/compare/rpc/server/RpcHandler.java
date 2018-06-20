package com.hzgc.compare.rpc.server;

import com.hzgc.compare.rpc.protocol.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {
    public RpcHandler(Map<String, Object> rpcServiceMap) {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {

    }
}
