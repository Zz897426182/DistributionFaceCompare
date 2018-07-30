package com.hzgc.compare.worker.memory.manager;

import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import sun.security.krb5.internal.rcache.MemoryCache;

import java.util.TimerTask;

public class ShowMemory extends TimerTask {

    @Override
    public void run() {
        MemoryCacheImpl.getInstance().showMemory();
    }
}
