package com.hzgc.compare.rpc.server.netty;

import com.hzgc.compare.rpc.protocol.JsonUtil;
import com.hzgc.compare.rpc.protocol.MsgType;
import com.hzgc.compare.rpc.protocol.RpcRequest;
import com.hzgc.compare.rpc.protocol.RpcResponse;
import com.hzgc.compare.rpc.server.RpcServer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

/**
 * server端handler，用来处理出入站消息
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);
    private final Map<String, Object> rpcServiceMap;
    private final Map<String, FastClass> fastClassMap;

    public RpcServerHandler(Map<String, Object> rpcServiceMap, Map<String, FastClass> fastClassMap) {
        this.rpcServiceMap = rpcServiceMap;
        this.fastClassMap = fastClassMap;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final RpcRequest rpcRequest) throws Exception {
        switch (rpcRequest.getType()) {
            case ASK:
                RpcServer.execute(() -> {
                    logger.debug("Receive request, request id is:{}", rpcRequest.getRequestId());
                    RpcResponse response = new RpcResponse();
                    response.setType(MsgType.ASK);
                    response.setRequestId(rpcRequest.getRequestId());
                    try {
                        Object result = handle(rpcRequest);
                        response.setResult(result);
                    } catch (Throwable throwable) {
                        response.setError(throwable.getMessage());
                        logger.error("Rpc server handle request error ", throwable);
                    }
                    ctx.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture ->
                            logger.debug("Send response for request " + JsonUtil.objectToJson(response)));
                });
                break;
            case PING:
                RpcResponse response = new RpcResponse();
                response.setType(MsgType.PONG);
                ctx.writeAndFlush(response);
                break;
        }

    }

    /**
     * 请求处理方法并返回处理结果
     *
     * @param rpcRequest 消息请求对象
     * @return 请求结果
     * @throws Throwable 可能抛出的异常
     */
    private Object handle(RpcRequest rpcRequest) throws Throwable {
        String className = rpcRequest.getClassName();
        if (rpcServiceMap.containsKey(className) && fastClassMap.containsKey(className)) {
            Object serviceBean = rpcServiceMap.get(className);
            FastClass fastClass = fastClassMap.get(className);
            String requestMethodName = rpcRequest.getMethodName();
            Class<?>[] requestParameterTypes = rpcRequest.getParameterTypes();
            Object[] requestParameters = rpcRequest.getParameters();
            FastMethod fastMethod = fastClass.getMethod(requestMethodName, requestParameterTypes);
            logger.debug("Call rpc service, name is:{}, method name is:{}", className, requestMethodName);
            logger.debug(Arrays.toString(requestParameterTypes));
            logger.debug(Arrays.toString(requestParameters));
            return fastMethod.invoke(serviceBean, requestParameters);
        } else {
            throw new Throwable("Rpc service implementation is not found, service name is:" + rpcRequest.getClassName());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Server caugth exception ", cause);
        ctx.close();
    }
}
