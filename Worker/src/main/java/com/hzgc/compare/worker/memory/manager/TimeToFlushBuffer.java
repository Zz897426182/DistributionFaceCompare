package com.hzgc.compare.worker.memory.manager;

import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl1;

import java.util.TimerTask;

public class TimeToFlushBuffer extends TimerTask{

    @Override
    public void run() {
        MemoryCacheImpl1.getInstance().flush();
    }
}
