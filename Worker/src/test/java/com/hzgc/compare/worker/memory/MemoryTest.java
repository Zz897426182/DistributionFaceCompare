package com.hzgc.compare.worker.memory;

import com.hzgc.compare.worker.CreateRecordToBuffer;
import com.hzgc.compare.worker.CreateRecordToKafka;
import com.hzgc.compare.worker.CreateRecordsToCach;
import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.tuple.Triplet;
import com.hzgc.compare.worker.common.taskhandle.FlushTask;
import com.hzgc.compare.worker.common.taskhandle.TaskToHandleQueue;
import com.hzgc.compare.worker.comsumer.Comsumer;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import com.hzgc.compare.worker.memory.manager.MemoryManager;
import com.sun.tools.javac.util.Assert;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MemoryTest {
    private Config config;
    private MemoryCacheImpl<String, String, byte[]> cache;
    private MemoryManager manager;
    private TaskToHandleQueue queue;
    @Before
    public void prepare(){
        config = Config.getConf();
        cache = MemoryCacheImpl.getInstance(config);
        manager = new MemoryManager<String, String, byte[]>();
        queue = TaskToHandleQueue.getTaskQueue();

    }


//    @Test
    public void testGetRecordsFromKafka(){
        Comsumer comsumer = new Comsumer();
        comsumer.start();
        try {
            CreateRecordToKafka.createRecords(1, 10000);
            Thread.sleep(3000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        List<FaceObject> faceObjectList = cache.getObjects();
        Assert.check(faceObjectList.size() == 10000);
    }

    /**
     * 测试处理buffer中的数据
     */
    @Test
    public void testDealWithBuffer(){
        try {
            CreateRecordToBuffer.createRecords(1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cache.flush();
        FlushTask task = queue.getTask(FlushTask.class);
        Assert.check(task != null);
        Assert.check(cache.getBuffer().size() == 0);
        Map<Triplet<String, String, String>, List<Pair<String, byte[]>>> map = cache.getCacheRecords();
        int count = 0;
        for(List list : map.values()){
            count += list.size();
        }
        Assert.check(count == 1000);
    }

    /**
     * 测试内存清理
     */
    @Test
    public void testRemove(){
        int sizeMax = 6000;
        int timeOut = 10;
        Config.getConf().setValue(Config.WORKER_RECORD_TIME_OUT, timeOut + "");
        Config.getConf().setValue(Config.WORKER_CACHE_SIZE_MAX, sizeMax + "");
        manager.reLoadParam();
        try {
            CreateRecordsToCach.createRecords(30, 500);
        } catch (IOException e) {
            e.printStackTrace();
        }
        manager.remove();
        Map<Triplet<String, String, String>, List<Pair<String, byte[]>>> map = cache.getCacheRecords();
        List<String> days = new ArrayList<>();
        for(Triplet<String, String, String> key : map.keySet()){
            String day = key.getThird();
            if(!days.contains(day)){
                days.add(day);
            }
        }
        Assert.check(days.size() <= 10);

        int count = 0;
        for(List<Pair<String, byte[]>> value : map.values()){
            count += value.size();
        }
        Assert.check(count <= sizeMax * 0.8);
    }
}
