package com.hzgc.compare.rpc.client.connect;

import com.hzgc.compare.rpc.client.netty.RpcClientHandler;
import com.hzgc.compare.rpc.protocol.RpcDecoder;
import com.hzgc.compare.rpc.protocol.RpcEncoder;
import com.hzgc.compare.rpc.protocol.RpcRequest;
import com.hzgc.compare.rpc.protocol.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline channelPipeline = socketChannel.pipeline();
        channelPipeline.addLast(new RpcEncoder(RpcRequest.class));
        channelPipeline.addLast(new LengthFieldBasedFrameDecoder(65536,
                0,
                4,
                0,
                0));
        channelPipeline.addLast(new RpcDecoder(RpcResponse.class));
        channelPipeline.addLast(new RpcClientHandler());
    }
}
