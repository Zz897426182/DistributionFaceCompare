package com.hzgc.compare.worker.persistence;



import com.hzgc.compare.worker.CreateRecordToCache1;
import com.hzgc.compare.worker.common.FaceInfoTable;
import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.Quintuple;
import com.hzgc.compare.worker.common.taskhandle.TaskToHandleQueue;

import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import com.hzgc.compare.worker.memory.manager.MemoryManager;
import com.hzgc.compare.worker.util.FaceObjectUtil;
import com.hzgc.compare.worker.util.HBaseHelper;
import com.sun.tools.javac.util.Assert;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class HBaseTest {
    private Config config;
    private MemoryCacheImpl cache;
    MemoryManager manager;
    TaskToHandleQueue queue;
    @Before
    public void prepare(){
        config = Config.getConf();
        cache = MemoryCacheImpl.<String, String, byte[]>getInstance(config);
        manager = new MemoryManager<String, String, byte[]>();
        queue = TaskToHandleQueue.getTaskQueue();

    }

    /**
     * 测试内存中的数据FaceObject数据持久化到HBase，并保存元数据到buffer
     */
    @Test
    public void testWriteHBase(){
        HBaseClient client = new HBaseClient();
        try {
            CreateRecordToCache1.createRecords(1, 1000);
            client.timeToWrite();
            Thread.sleep(6000);
            List<Quintuple<String, String, String, String, byte[]>> buffer = cache.getBuffer();
            Assert.check(buffer.size() == 1000);

            Table table = HBaseHelper.getTable(FaceInfoTable.TABLE_NAME);
            List<Get> gets = new ArrayList<>();
            for(Quintuple<String, String, String, String, byte[]> quintuple : buffer){
                Get get = new Get(Bytes.toBytes(quintuple.getFourth()));
                gets.add(get);
            }
            List<FaceObject> list = new ArrayList<>();
            Result[] results = table.get(gets);
            for (Result res : results){//对返回的结果集进行操作
                for (Cell kv : res.rawCells()) {
                    FaceObject value = FaceObjectUtil.jsonToObject(Bytes.toString(CellUtil.cloneValue(kv))) ;
                    Assert.checkNonNull(value);
                    list.add(value);
                }
            }
            Assert.check(list.size() == 1000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
