package com.hzgc.compare.worker.persistence.task;

import com.hzgc.compare.worker.common.FaceInfoTable;
import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.Quintuple;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl1;
import com.hzgc.compare.worker.util.FaceObjectUtil;
import com.hzgc.compare.worker.util.HBaseHelper;
import com.hzgc.compare.worker.util.UuidUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 定期读取内存中的recordToHBase，保存在HBase中，并生成元数据保存入内存的buffer
 */
public class TimeToWrite extends Thread{
    private Config conf;
    private Long timeToWrite = 1000L; //任务执行时间间隔，默认1秒

    public TimeToWrite(Config conf){
        this.conf = conf;
        this.timeToWrite = conf.getValue(Config.WORKER_HBASE_WRITE_TIME, this.timeToWrite);
    }
    public void run() {
        while (true) {
            try {
                Thread.sleep(timeToWrite);
                MemoryCacheImpl1 cache = MemoryCacheImpl1.getInstance(conf);
                List<FaceObject> recordToHBase = cache.getObjects();
                System.out.println("The record num from kafka is :" + recordToHBase.size());
                List<Quintuple<String, String, String, String, byte[]>> bufferList = new ArrayList<>();
                try {
                    List<Put> putList = new ArrayList<>();
                    Table table = HBaseHelper.getTable(FaceInfoTable.TABLE_NAME);
                    for (FaceObject record : recordToHBase) {
                        String rowkey = record.getDate() + "-" + record.getIpcId() + UuidUtil.getUuid().substring(0, 24);
                        Put put = new Put(Bytes.toBytes(rowkey));
                        put.addColumn(Bytes.toBytes("face"), Bytes.toBytes("object"), Bytes.toBytes(FaceObjectUtil.objectToJson(record)));
                        putList.add(put);
                        Quintuple<String, String, String, String, byte[]> bufferRecord =
                                new Quintuple<>(record.getIpcId(), null, record.getDate(), rowkey, record.getAttribute().getFeature2());
                        bufferList.add(bufferRecord);
                    }
                    table.put(putList);
                } catch (IOException e) {
                    e.printStackTrace();
                    cache.recordToHBase(recordToHBase);
                }
                cache.addBuffer(bufferList);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
