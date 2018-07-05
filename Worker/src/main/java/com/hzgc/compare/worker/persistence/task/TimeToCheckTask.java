package com.hzgc.compare.worker.persistence.task;

import com.hzgc.compare.worker.conf.Config;

import java.util.TimerTask;

/**
 * 定期查看TaskToHandle中有无FlushTask，如果有，则flush其中的记录，并删除该FlushTask
 */
public class TimeToCheckTask extends TimerTask{
    private Config conf;
    public TimeToCheckTask(Config conf){

    }
    public void run() {

    }
}
