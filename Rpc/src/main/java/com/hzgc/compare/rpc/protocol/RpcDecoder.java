package com.hzgc.compare.rpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.concurrent.EventExecutorGroup;
import sun.reflect.Reflection;

import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {
    public RpcDecoder(Class<RpcRequest> rpcRequestClass) {
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
    }
}
