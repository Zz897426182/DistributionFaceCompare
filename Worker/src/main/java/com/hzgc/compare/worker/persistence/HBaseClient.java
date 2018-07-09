package com.hzgc.compare.worker.persistence;

import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.SearchResult;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.persistence.task.TimeToWrite;
import javafx.util.Pair;

import java.util.List;
import java.util.Timer;


/**
 * 负责与HBas交互，定期插入数据，以及读取第一次比较结果
 */
public class HBaseClient {
    private Config conf;
    private Long timeToWrite = 1000L; //任务执行时间间隔，默认1秒

    public HBaseClient(Config conf){
        this.conf = conf;
    }

    /**
     * 启动任务，定期读取内存中的recordToHBase，保存在HBase中，并生成元数据保存入内存的buffer
     */
    public void timeToWrite(){
        long time = conf.getValue(Config.WORKER_HBASE_WRITE_TIME, 1000L);
        TimeToWrite task = new TimeToWrite(conf);
        new Timer().schedule(task, time, time);
    }

    /**
     * 根据过滤结果，查询HBase中的数据
     * @param records
     * @return
     */
    public List<Pair<String, float[]>> readFromHBase(List<String> records){
        return null;
    }

    /**
     * 根据第一次比较的结果，查询HBase中的数据
     * @param records
     * @return
     */
    public List<Pair<String, float[]>> readFromHBase2(List<Pair<String, byte[]>> records){
        return null;
    }

    /**
     * 对比结束，根据结果查询HBase数据
     * @param record
     * @return
     */
    public List<FaceObject> readFromHBase2(SearchResult record){
        return null;
    }
}
