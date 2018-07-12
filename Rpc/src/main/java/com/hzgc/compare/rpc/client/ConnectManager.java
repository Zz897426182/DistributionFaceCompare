package com.hzgc.compare.rpc.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hzgc.compare.rpc.client.netty.ClientChannelInitializer;
import com.hzgc.compare.rpc.client.netty.RpcClientHandler;
import com.hzgc.compare.rpc.util.RpcConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 连接管理类,主要用来管理客户端与服务端的连接
 * 包括服务端某个节点与客户端节点断开连接则移除以及新增服务节点后进行主动连接
 */
public class ConnectManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectManager.class);
    private static volatile ConnectManager instance;
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    //RpcClientHandler管理容器
    private CopyOnWriteArrayList<RpcClientHandler> connectedHandlers = Lists.newCopyOnWriteArrayList();
    //服务端地址对象和RpcClientHandler对象的映射
    private Map<InetSocketAddress, RpcClientHandler> connectedServerNodes = Maps.newConcurrentMap();
    //在未获取到服务端节点时进行阻塞,在获取到服务器端节点时通知
    private ReentrantLock lock = new ReentrantLock();
    //ReentranLock产生的Condition对象用来做阻塞和唤醒
    private Condition connected = lock.newCondition();
    //线程安全的整数
    private AtomicInteger roundRobin = new AtomicInteger(0);
    //是否运行
    private volatile boolean isRuning = true;

    private ConnectManager() {
    }

    //单例获取
    public static ConnectManager getInstance() {
        if (instance == null) {
            //同步加锁保证线程安全
            synchronized (ConnectManager.class) {
                //获得锁之后再次进行判断是否为空
                if (instance == null) {
                    instance = new ConnectManager();
                }
            }
        }
        return instance;
    }

    /**
     * 当节点发现工具观察到了zk节点的变化就会将当前节点下最新的信息收集成为集合然后调用此方法
     *
     * @param allServerddress 当前zk节点下最新的信息的集合
     */
    public void updateConnectedServer(List<String> allServerddress) {
        if (allServerddress != null && allServerddress.size() > 0) {
            //将最新的节点信息映射为InetSocketAddress
            List<InetSocketAddress> newAllServerNodeList = allServerddress.stream().map(address -> {
                String[] array = address.split(":");
                if (array.length == 2) {
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    return new InetSocketAddress(host, port);
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            //对比之前连接信息进行新增节点连接或者删除
            for (final InetSocketAddress serverNodeAddress : newAllServerNodeList) {
                RpcClientHandler rpcClientHandler = connectedServerNodes.get(serverNodeAddress);
                //如果连接信息池里面没有此连接则为新连接,需要与服务端建立连接
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
                InetSocketAddress remotePeer = connectedServerHandler.getRemotePeer();
                //如果之前的连接不存在于最新的可用的连接里面则在连接管理池中去除
                if (!newAllServerNodeList.contains(remotePeer)) {
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
                InetSocketAddress remotePeer = connectedServerHandler.getRemotePeer();
                RpcClientHandler handler = connectedServerNodes.get(remotePeer);
                handler.close();
                connectedServerNodes.remove(remotePeer);
            }
            connectedHandlers.clear();
        }
    }

    // FIXME: 18-7-5 
    public void reconnect(final RpcClientHandler handler, final SocketAddress remotePeer) {
        if (handler != null) {
            connectedHandlers.remove(handler);
            connectedServerNodes.remove(handler.getRemotePeer());
        }
        connectServerNode((InetSocketAddress) remotePeer);
    }

    /**
     * 连接远程服务端
     *
     * @param remotePeer 远程连接对象包括了ip和port
     */
    private void connectServerNode(final InetSocketAddress remotePeer) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .handler(new ClientChannelInitializer());
        try {
            ChannelFuture channelFuture = bootstrap.connect(remotePeer).sync();
            final CountDownLatch latch = new CountDownLatch(1);
            channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
                if (channelFuture1.isSuccess()) {
                    logger.info("Successfully connect to remote server, remote peer = {}", remotePeer);
                    RpcClientHandler handler = channelFuture1.channel().pipeline().get(RpcClientHandler.class);
                    addHandler(handler);
                    latch.countDown();
                }
            });
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将RpcClientHandler加入至连接管理池集合中
     *
     * @param handler 新增RpcClientHandler
     */
    private void addHandler(RpcClientHandler handler) {
        logger.info("Add RpcClientHandler into connection manager");
        connectedHandlers.add(handler);
        InetSocketAddress remoteAddress = (InetSocketAddress) handler.getChannel().remoteAddress();
        connectedServerNodes.put(remoteAddress, handler);
        signalAvailableHandler();
    }

    /**
     * 在ConnectManager检测到没有任何注册在zk上面的服务就会被阻塞且等待被唤醒
     * 而此处会唤醒被阻塞的程序
     */
    private void signalAvailableHandler() {
        lock.lock();
        try {
            logger.info("Found a new connection, send a handler signal available");
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 在ConnectManager检测到没有任何注册在zk上面的服务就会被阻塞且等待被唤醒
     * 而在此处程序会被阻塞,此时使用了超时阻塞,如果指定时间未等待成功则继续执行且返回false
     *
     * @return 是否等待成功
     * @throws InterruptedException 可能抛出的异常
     */
    private boolean waitingForHandler() throws InterruptedException {
        return connected.await(RpcConfig.getRpcConnectManagerConnectTimeout(), TimeUnit.MILLISECONDS);
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

    public void stop() {
        isRuning = false;
        for (RpcClientHandler connectedServerHandler : connectedHandlers) {
            connectedServerHandler.close();
        }
        signalAvailableHandler();
        eventLoopGroup.shutdownGracefully();
    }

    /**
     * 移除失效或者断开连接的handler从RpcClientHandler连接信息池中
     * 此方法会在removeCurrentHandler()方法中被调用
     *
     * @param handler 失效的RpcClientHandler
     */
    public void removeRpcClientHandler(RpcClientHandler handler) {
        if (connectedHandlers.contains(handler)) {
            logger.info("ConnectedHandlers contains this invalid handler:{}, remove it", handler.toString());
            connectedHandlers.remove(handler);
        } else {
            logger.warn("ConnectedHandlers is not contains this invalid handler:{}", handler.toString());
        }
    }

    /**
     * 移除失效或者断开连接的handler从InetSocketAddress映射RpcClientHandler连接信息池中
     * 此方法会在removeCurrentHandler()方法中被调用
     *
     * @param socketAddress 远程连接地址对象
     */
    public void removeConnectedServerNodes(InetSocketAddress socketAddress) {
        if (connectedServerNodes.containsKey(socketAddress)) {
            logger.info("ConnectedServerNodes contains this invalid socketAddress:{}, remove it", socketAddress.toString());
            connectedServerNodes.remove(socketAddress);
        } else {
            logger.warn("ConnectedServerNodes contains this invalid socketAddress:{}", socketAddress.toString());
        }
    }

    public CopyOnWriteArrayList<RpcClientHandler> getConnectedHandlers() {
        return connectedHandlers;
    }
}
