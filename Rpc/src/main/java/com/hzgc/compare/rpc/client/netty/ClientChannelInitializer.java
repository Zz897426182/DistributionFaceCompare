package com.hzgc.compare.rpc.client.netty;

import com.hzgc.compare.rpc.protocol.RpcDecoder;
import com.hzgc.compare.rpc.protocol.RpcEncoder;
import com.hzgc.compare.rpc.protocol.RpcRequest;
import com.hzgc.compare.rpc.protocol.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline channelPipeline = socketChannel.pipeline();
        //添加心跳机制
        channelPipeline.addLast(new IdleStateHandler(0,
                4,
                0,
                TimeUnit.SECONDS));
        //添加编码器
        channelPipeline.addLast(new RpcEncoder(RpcRequest.class));
        //解决粘包问题
        channelPipeline.addLast(new LengthFieldBasedFrameDecoder(65536,
                0,
                4,
                0,
                0));
        //粘包处理后进行解码
        channelPipeline.addLast(new RpcDecoder(RpcResponse.class));
        channelPipeline.addLast(new RpcClientHandler());
    }
}
