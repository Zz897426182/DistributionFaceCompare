package com.hzgc.compare.worker.memory.cache;

import com.hzgc.compare.worker.common.*;
import com.hzgc.compare.worker.common.taskhandle.FlushTask;
import com.hzgc.compare.worker.common.taskhandle.TaskToHandleQueue;
import com.hzgc.compare.worker.conf.Config;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内存缓存模块，单例模式，内部存储三种数据buffer和cacheRecords，以及recordToHBase
 * 从kafka读入的数据先存储在recordToHBase，再由持久化模块不断将recordToHBase中的数据存入HBase中，然后生成元数据，将它保存在buffer中
 * 当buffer数据量达到一定时，将buffer持久化，并加入cacheRecords，buffer清空
 */
public class MemoryCacheImpl1 implements MemoryCache<Map<Triplet<String, String, String>, List<Pair<String, byte[]>>>>{
    private static MemoryCacheImpl1 memoryCache;
    private Config conf;
    private int flushProgram = 0; //flush 方案 0 定期flush  1 定量flush
    private Integer bufferSizeMax = 1000; // buffer存储上限，默认500
    private DoubleBufferQueue<FaceObject> recordToHBase; //这里应该是一个类似阻塞队列的集合
    private Map<Triplet<String, String, String>, List<Pair<String, byte[]>>> cacheRecords;
    private DoubleBufferQueue<Quintuple<String, String, String, String, byte[]>> buffer;


    private MemoryCacheImpl1(Config conf){
        this.conf = conf;
        init(conf);
    }

    public static MemoryCacheImpl1 getInstance(Config conf){
        if(memoryCache == null){
            memoryCache = new MemoryCacheImpl1(conf);
        }
        return memoryCache;
    }

    public static MemoryCacheImpl1 getInstance(){
        if(memoryCache == null){
            Config config = Config.getConf();
            memoryCache = new MemoryCacheImpl1(config);
        }
        return memoryCache;
    }

    private void init(Config conf) {
        bufferSizeMax = conf.getValue(Config.WORKER_BUFFER_SIZE_MAX, bufferSizeMax);
        recordToHBase = new DoubleBufferQueue<>();
        cacheRecords = new HashMap<>();
        buffer = new DoubleBufferQueue<>();
    }

    /**
     * 返回recordToHBase
     * @return
     */
    public List<FaceObject> getObjects() {
        List<FaceObject>objs =  recordToHBase.get();
        return  objs;
    }

    public List<Quintuple<String, String, String, String, byte[]>> getBuffer(){
        return buffer.get();
    }

    /**
     * 返回cacheRecords
     * @return
     */
    public Map<Triplet<String, String, String>, List<Pair<String, byte[]>>> getCacheRecords() {
        return cacheRecords;
    }

    @Override
    public void setBufferSizeMax(int size) {
        this.bufferSizeMax = size;
    }

    /**
     * 增加recordToHBase
     */
    public void recordToHBase(List<FaceObject> objs) {
        if(objs.size() > 0) {
            recordToHBase.push(objs);
        }
    }

    /**
     * 增加多条record
     * @param records
     */
    public void loadCacheRecords(Map<Triplet<String, String, String>, List<Pair<String, byte[]>>> records) {
        for(Map.Entry<Triplet<String, String, String>, List<Pair<String, byte[]>>> entry : records.entrySet()){
            Triplet<String, String, String> key = entry.getKey();
            List<Pair<String, byte[]>> value = entry.getValue();
            List<Pair<String, byte[]>> list = cacheRecords.get(key);
            if(list == null || list.size() == 0){
                cacheRecords.put(key, value);
            }else {
                list.addAll(value);
            }
        }
    }

    /**
     * 将多条记录加入buffer，然后检查buffer是否满了
     * @param records
     */
    public void addBuffer(List<Quintuple<String, String, String, String, byte[]>> records) {
//        if(buffer == null || buffer.size() == 0){
//            buffer = records;
//        }else {
//            buffer.addAll(records);
//        }
        buffer.push(records);
        if(flushProgram == 1){
            check();
        }
    }

    /**
     * 检查buffer是否满了, 如果满了，则在TaskToHandle中添加一个FlushTask任务,并将buffer加入cacheRecords，buffer重新创建
     */
    public void check() {
        System.out.println("To check The Buferr if it is to be flushed.");
        if(buffer.getWriteListSize() >= bufferSizeMax){
            TaskToHandleQueue.getTaskQueue().addTask(new FlushTask(buffer.getWithoutRemove()));
            moveToCacheRecords(buffer.get());
        }
    }

    public void flush(){
        if(buffer.getWriteListSize() > 0) {
            System.out.println("To flush the buffer.");
            TaskToHandleQueue.getTaskQueue().addTask(new FlushTask(buffer.getWithoutRemove()));
            moveToCacheRecords(buffer.get());
        }
    }

    /**
     * 将数据加入cacheRecords
     */
    public void moveToCacheRecords(List<Quintuple<String, String, String, String, byte[]>> records) {
        for(Quintuple<String, String, String, String, byte[]> record : records){
            Triplet<String, String, String> key =
                    new Triplet<>(record.getFirst(), record.getSecond(), record.getThird());

            Pair<String, byte[]> value = new Pair<>(record.getFourth(), record.getFifth());
            List<Pair<String, byte[]>> list = cacheRecords.get(key);
            if(list == null){
                list = new ArrayList<>();
                cacheRecords.put(key, list);
            }
            list.add(value);
        }
    }
}
