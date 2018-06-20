package com.hzgc.compare.rpc.registry;

import org.junit.Before;
import org.junit.Test;

public class ServiceRegistryTest {

    private ServiceRegistry serviceRegistry;

    @Before
    public void init() {
        serviceRegistry = new ServiceRegistry("172.18.18.105:2181");
    }

    @Test
    public void register() {
        serviceRegistry.register("172.18.18.105:2181");
    }
}
