package com.hzgc.compare.worker.memory.manager;

import java.util.TimerTask;

public class TimeToCheckMemory extends TimerTask {
    private MemoryManager manager;
    public TimeToCheckMemory(MemoryManager manager){
        this.manager = manager;
    }

    public void run() {
        manager.remove();
    }
}
