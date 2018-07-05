package com.hzgc.compare.worker.persistence.task;

import com.hzgc.compare.worker.conf.Config;

import java.util.TimerTask;

/**
 * 定期读取内存中的recordToHBase，保存在HBase中，并生成元数据保存入内存的buffer
 */
public class TimeToWrite extends TimerTask{
    private Config conf;
    public TimeToWrite(Config conf){

    }
    public void run() {

    }
}
