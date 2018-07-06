package com.hzgc.compare.worker.persistence.task;

import com.hzgc.compare.worker.conf.Config;

import java.util.TimerTask;

/**
 * 定期任务，检查文件是否存在过期，并删除过期文件
 */
public class TimeToCheckFile extends TimerTask{
    private Config conf;
    private Long fileTimeOut = 1000L * 60 * 60 * 24 * 150; //文件过期时间， 默认5个月

    public TimeToCheckFile(Config conf){

    }
    @Override
    public void run() {

    }
}
