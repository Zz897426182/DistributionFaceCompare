package com.hzgc.compare.rpc.server.annotation;

import java.lang.annotation.*;

/**
 * 服务端所有提供接口实现的类需要使用此注解
 * ex: @RpcService(InterfaceName.class)
 * 此处使用接口类的类对象而不是实现类的类对象
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RpcService {
    Class<?> value();
}
