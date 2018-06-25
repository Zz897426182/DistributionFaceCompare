package com.hzgc.compare.rpc.server.netty;

import com.hzgc.compare.rpc.protocol.RpcRequest;
import com.hzgc.compare.rpc.protocol.RpcResponse;
import com.hzgc.compare.rpc.server.RpcServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RpcHandler.class);
    private final Map<String, Object> rpcServiceMap;
    private final Map<String, FastClass> fastClassMap;

    public RpcHandler(Map<String, Object> rpcServiceMap, Map<String, FastClass> fastClassMap) {
        this.rpcServiceMap = rpcServiceMap;
        this.fastClassMap = fastClassMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, final RpcRequest rpcRequest) throws Exception {
        RpcServer.execute(() -> {
            logger.info("Receive request, request id is:{}", rpcRequest.getRequestId());
            RpcResponse response = new RpcResponse();
            response.setRequestId(rpcRequest.getRequestId());
            try {
                Object result = handle(rpcRequest);
                response.setResult(result);
            } catch (Throwable throwable) {
                response.setError(throwable.getMessage());
                logger.error("Rpc server handle request error ", throwable);
            }
        });
    }

    private Object handle(RpcRequest rpcRequest) throws Throwable {
        String className = rpcRequest.getClassName();
        if (rpcServiceMap.containsKey(className) && fastClassMap.containsKey(className)) {
            Object serviceBean = rpcServiceMap.get(className);
            FastClass fastClass = fastClassMap.get(className);
            String requestMethodName = rpcRequest.getMethodName();
            Class<?>[] requestParameterTypes = rpcRequest.getParameterTypes();
            Object[] requestParameters = rpcRequest.getParameters();
            FastMethod fastMethod = fastClass.getMethod(requestMethodName, requestParameterTypes);
            logger.info("Call rpc service, name is:{}, method name is:{}", className, requestMethodName);
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
