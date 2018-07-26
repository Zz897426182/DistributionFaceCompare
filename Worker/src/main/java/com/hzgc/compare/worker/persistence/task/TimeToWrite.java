package com.hzgc.compare.worker.persistence.task;

import com.hzgc.compare.worker.common.FaceInfoTable;
import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.Quintuple;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import com.hzgc.compare.worker.util.FaceObjectUtil;
import com.hzgc.compare.worker.util.HBaseHelper;
import com.hzgc.compare.worker.util.UuidUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 定期读取内存中的recordToHBase，保存在HBase中，并生成元数据保存入内存的buffer
 */
public class TimeToWrite implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(TimeToWrite.class);
    private Config conf;
    private Long timeToWrite = 1000L; //任务执行时间间隔，默认1秒

    public TimeToWrite(){
        this.conf = Config.getConf();
        this.timeToWrite = conf.getValue(Config.WORKER_HBASE_WRITE_TIME, this.timeToWrite);
    }
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(timeToWrite);
                writeToHBase();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeToHBase(){
        logger.info("To Write record into HBase.");
        MemoryCacheImpl<String, String, byte[]> cache = MemoryCacheImpl.getInstance();
        List<FaceObject> recordToHBase = cache.getObjects();
        if(recordToHBase.size() == 0){
            return;
        }
        logger.info("The record num from kafka is :" + recordToHBase.size());
        long start = System.currentTimeMillis();
        List<Quintuple<String, String, String, String, byte[]>> bufferList = new ArrayList<>();
        try {
            List<Put> putList = new ArrayList<>();
            Table table = HBaseHelper.getTable(FaceInfoTable.TABLE_NAME);
            for (FaceObject record : recordToHBase) {
                String rowkey = record.getDate() + "-" + record.getIpcId() + UuidUtil.getUuid().substring(0, 24);
                Put put = new Put(Bytes.toBytes(rowkey));
                put.addColumn(Bytes.toBytes(FaceInfoTable.CLU_FAMILY), Bytes.toBytes(FaceInfoTable.INFO), Bytes.toBytes(FaceObjectUtil.objectToJson(record)));
                putList.add(put);
                Quintuple<String, String, String, String, byte[]> bufferRecord =
                        new Quintuple<>(record.getIpcId(), null, record.getDate(), rowkey, record.getAttribute().getFeature2());
                bufferList.add(bufferRecord);
            }
            table.put(putList);
            cache.addBuffer(bufferList);
            logger.info("Put record to hbase success .");
            logger.info("The Time Used to write to HBase is : " + (System.currentTimeMillis()  - start));
        } catch (IOException e) {
            e.printStackTrace();
            cache.addFaceObjects(recordToHBase);
            logger.error("Put records to hbase faild.");
        }
    }
}
