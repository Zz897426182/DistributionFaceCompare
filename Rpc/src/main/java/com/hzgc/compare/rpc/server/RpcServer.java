package com.hzgc.compare.rpc.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hzgc.compare.rpc.protocol.RpcDecoder;
import com.hzgc.compare.rpc.protocol.RpcEncoder;
import com.hzgc.compare.rpc.protocol.RpcRequest;
import com.hzgc.compare.rpc.protocol.RpcResponse;
import com.hzgc.compare.rpc.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcServer {
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);
    private String ipAddress;
    private int port;
    private ServiceRegistry serviceRegistry;
    private Map<String, Object> rpcServiceMap = Maps.newHashMap();
    private static ThreadPoolExecutor threadPoolExecutor;


    public RpcServer(String ipAddress, int port, ServiceRegistry serviceRegistry) {
        this(ipAddress, port, serviceRegistry, Lists.newArrayList());
    }

    public RpcServer(String ipAddress, int port, ServiceRegistry serviceRegistry, List<String> filterList) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.serviceRegistry = serviceRegistry;
        scanRpcService(filterList);
    }


    public static void execute(Runnable task) {
        if (threadPoolExecutor == null) {
            synchronized (RpcServer.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(16,
                            16,
                            600L,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<Runnable>(65535));
                }
            }
        }
        threadPoolExecutor.execute(task);
    }

    private void scanRpcService(List<String> filterList) {
        RpcServiceScanner serviceScanner = new RpcServiceScanner();
        List<Class<?>> classList;
        if (filterList == null || filterList.size() == 0) {
            classList = serviceScanner.scanner();
        } else {
            classList = serviceScanner.scanner(filterList);
        }
        if (classList.size() == 0) {
            logger.warn("The Rpc Service implementation class is not scanned");
        }
        registRpcService(classList);
    }

    private void registRpcService(List<Class<?>> classList) {
        logger.info("Start regist rpc service implementation into rpcServiceMap");
        for (Class<?> clz : classList) {
            try {
                rpcServiceMap.put(clz.getName(), clz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(65535,
                                        0,
                                        4,
                                        0,
                                        0))
                                .addLast(new RpcDecoder(RpcRequest.class))
                                .addLast(new RpcEncoder(RpcResponse.class))
                                .addLast(new RpcHandler(rpcServiceMap));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            ChannelFuture future = bootstrap.bind(this.ipAddress, this.port).sync();
            logger.info("Rpc server started on {}, bind ip on {}", this.port, this.ipAddress);
            if (this.serviceRegistry != null) {
                serviceRegistry.register(this.ipAddress + ":" + this.port);
            }
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }
}
