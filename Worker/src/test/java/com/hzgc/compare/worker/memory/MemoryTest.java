package com.hzgc.compare.worker.memory;

import com.hzgc.compare.worker.CreateRecordToBuffer;
import com.hzgc.compare.worker.CreateRecordToCache1;
import com.hzgc.compare.worker.CreateRecordToKafka;
import com.hzgc.compare.worker.CreateRecordsToCach2;
import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.Triplet;
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
import java.util.List;
import java.util.Map;

public class MemoryTest {
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

    @Test
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

    @Test
    public void testDealWithBuffer(){
        CreateRecordToBuffer.createRecords(1000);
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

    @Test
    public void testRemove(){
        CreateRecordsToCach2.createRecords();
        manager.remove();

    }
}
