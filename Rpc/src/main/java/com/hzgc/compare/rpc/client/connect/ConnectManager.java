package com.hzgc.compare.rpc.client.connect;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hzgc.compare.rpc.protocol.RpcDecoder;
import com.hzgc.compare.rpc.protocol.RpcEncoder;
import com.hzgc.compare.rpc.protocol.RpcRequest;
import com.hzgc.compare.rpc.protocol.RpcResponse;
import com.hzgc.compare.rpc.server.annotation.RpcService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectManager.class);
    private static volatile ConnectManager instance;
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16,
            16,
            600L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(65535));
    CopyOnWriteArrayList<RpcClientHandler> connectedHandlers = Lists.newCopyOnWriteArrayList();
    Map<InetSocketAddress, RpcClientHandler> connectedServerNodes = Maps.newConcurrentMap();
    private ReentrantLock lock = new ReentrantLock();
    private Condition connected = lock.newCondition();
    private AtomicInteger roundRobin = new AtomicInteger(0);
    private volatile boolean isRuning = true;

    private ConnectManager() {
    }

    public static ConnectManager getInstance() {
        if (instance == null) {
            synchronized (ConnectManager.class) {
                if (instance == null) {
                    instance = new ConnectManager();
                }
            }
        }
        return instance;
    }

    public void updateConnectedServer(List<String> allServerddress) {
        if (allServerddress != null && allServerddress.size() > 0) {
            HashSet<InetSocketAddress> newAllServerNodeSet = Sets.newHashSet();
            for (String address : allServerddress) {
                String[] array = address.split(":");
                if (array.length == 2) {
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    InetSocketAddress remotePeer = new InetSocketAddress(host, port);
                    newAllServerNodeSet.add(remotePeer);
                }
            }
            // FIXME: 18-6-27
            //需要考虑handler里面存在网络断开链接的情况
            for (final InetSocketAddress serverNodeAddress : newAllServerNodeSet) {
                RpcClientHandler rpcClientHandler = connectedServerNodes.get(serverNodeAddress);
                if (rpcClientHandler == null) {
                    connectServerNode(serverNodeAddress);
                } else {
                    logger.warn("Current server node address already exists, host is {}, port is {}",
                            serverNodeAddress.getHostString(),
                            serverNodeAddress.getPort());
                }
            }

            for (int i = 0; i < connectedHandlers.size(); i++) {
                RpcClientHandler connectedServerHandler = connectedHandlers.get(i);
                SocketAddress remotePeer = connectedServerHandler.getRemotePeer();
                if (!newAllServerNodeSet.contains(remotePeer)) {
                    logger.info("Remove invalid server node {}", remotePeer);
                    RpcClientHandler handler = connectedServerNodes.get(remotePeer);
                    if (handler != null) {
                        handler.close();
                    }
                    connectedServerNodes.remove(remotePeer);
                    connectedHandlers.remove(connectedServerHandler);
                }
            }
        } else {
            logger.error("No avaliable server node, all server nodes are down or not start");
            for (final RpcClientHandler connectedServerHandler : connectedHandlers) {
                SocketAddress remotePeer = connectedServerHandler.getRemotePeer();
                RpcClientHandler handler = connectedServerNodes.get(remotePeer);
                handler.close();
                connectedServerNodes.remove(connectedServerHandler);
            }
            connectedHandlers.clear();
        }
    }

    public void reconnect(final RpcClientHandler handler, final SocketAddress remotePeer) {
        if (handler != null) {
            connectedHandlers.remove(handler);
            connectedServerNodes.remove(handler.getRemotePeer());
        }
        connectServerNode((InetSocketAddress) remotePeer);
    }

    private void connectServerNode(final InetSocketAddress remotePeer) {
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .handler(new ChannelInitializer<SocketChannel>() {
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
                        });
                ChannelFuture channelFuture = bootstrap.connect(remotePeer);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            logger.info("Successfully connect to remote server, remote peer = {}", remotePeer);
                            RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                            addHandler(handler);
                        }
                    }
                });
            }
        });
    }

    private void addHandler(RpcClientHandler handler) {
        connectedHandlers.add(handler);
        InetSocketAddress remoteAddress = (InetSocketAddress) handler.getChannel().remoteAddress();
        connectedServerNodes.put(remoteAddress, handler);
        signalAvailableHandler();
    }

    private void signalAvailableHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean waitingForHandler() throws InterruptedException {
        long connectTimeoutMillis = 6000;
        return connected.await(connectTimeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 选择一个可用RPC服务,当前在选择服务时只使用了轮询的方式
     *
     * @return 可用RPC服务
     */
    public RpcClientHandler chooseHandler() {
        lock.lock();
        try {
            int size = connectedHandlers.size();
            while (isRuning && size <= 0) {
                try {
                    logger.warn("Waiting for available node, current available node size is 0");
                    boolean available = waitingForHandler();
                    if (available) {
                        size = connectedHandlers.size();
                    }
                } catch (InterruptedException e) {
                    logger.error("Waiting for available node is interrupted", e);
                    throw new RuntimeException("Cant't connect any servers", e);
                }
            }
            int index = (roundRobin.getAndAdd(1) + size) % size;
            return connectedHandlers.get(index);
        } finally {
            lock.unlock();
        }

    }

    void stop() {
        isRuning = false;
        for (RpcClientHandler connectedServerHandler : connectedHandlers) {
            connectedServerHandler.close();
        }
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }

    void removeRpcClientHandler(RpcClientHandler handler) {
        if (connectedHandlers.contains(handler)) {
            logger.info("ConnectedHandlers contains this invalid handler:{}, remove it", handler.toString());
            connectedHandlers.remove(handler);
        } else {
            logger.warn("ConnectedHandlers is not contains this invalid handler:{}", handler.toString());
        }
    }

    void removeConnectedServerNodes(InetSocketAddress socketAddress) {
        if (connectedServerNodes.containsKey(socketAddress)) {
            logger.info("ConnectedServerNodes contains this invalid socketAddress:{}, remove it", socketAddress.toString());
            connectedServerNodes.remove(socketAddress);
        } else {
            logger.warn("ConnectedServerNodes contains this invalid socketAddress:{}", socketAddress.toString());
        }
    }
}
