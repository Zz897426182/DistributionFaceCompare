package com.hzgc.compare.rpc.server;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class RpcServiceScannerTest {
    private RpcServiceScanner scanner;

    @Before
    public void before() {
        scanner = new RpcServiceScanner();
    }


    @Test
    public void scanner() {
        List<Class<?>> classList = scanner.scanner();
        Assert.assertTrue(classList.size() > 0);
        Assert.assertTrue(classList.get(0).isAnnotationPresent(RpcService.class));
    }
}
