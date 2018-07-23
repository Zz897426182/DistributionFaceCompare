package com.hzgc.compare.worker.memory.manager;

import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;

import java.util.TimerTask;

public class TimeToFlushBuffer extends TimerTask{

    @Override
    public void run() {
        MemoryCacheImpl.getInstance().flush();
    }
}
