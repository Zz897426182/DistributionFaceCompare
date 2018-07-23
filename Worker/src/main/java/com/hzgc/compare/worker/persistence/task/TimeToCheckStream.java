package com.hzgc.compare.worker.persistence.task;

import com.hzgc.compare.worker.conf.Config;

import java.util.TimerTask;

/**
 * 定期任务，检查是否存在过期的stream，过期删除
 */
public class TimeToCheckStream extends TimerTask {
    private Config conf;
    private Long streamTimeOut = 1000L * 60 * 60 * 24 * 10; //stream过期时间，默认10天
    public TimeToCheckStream(){
        this.conf = Config.getConf();
    }
    public void run() {

    }
}
