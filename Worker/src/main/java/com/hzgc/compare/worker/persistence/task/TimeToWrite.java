package com.hzgc.compare.worker.persistence.task;

import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl1;
import com.hzgc.compare.worker.util.HBaseHelper;
import org.apache.hadoop.hbase.client.Connection;

import java.util.List;
import java.util.TimerTask;

/**
 * 定期读取内存中的recordToHBase，保存在HBase中，并生成元数据保存入内存的buffer
 */
public class TimeToWrite extends TimerTask{
    private Config conf;
    public TimeToWrite(Config conf){
        this.conf = conf;
    }
    public void run() {
        MemoryCacheImpl1 cache = MemoryCacheImpl1.getInstance(conf);
        List<FaceObject> recordToHBase = cache.getObjects();
        Connection conn = HBaseHelper.getHBaseConnection();
    }
}
