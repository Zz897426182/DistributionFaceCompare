package com.hzgc.compare.worker.memory.cache;

import com.hzgc.compare.worker.common.*;
import com.hzgc.compare.worker.common.collects.BatchBufferQueue;
import com.hzgc.compare.worker.common.collects.CustomizeArrayList;
import com.hzgc.compare.worker.common.collects.DoubleBufferQueue;
import com.hzgc.compare.worker.common.taskhandle.FlushTask;
import com.hzgc.compare.worker.common.taskhandle.TaskToHandleQueue;
import com.hzgc.compare.worker.common.tuple.Quintuple;
import com.hzgc.compare.worker.common.tuple.Triplet;
import com.hzgc.compare.worker.conf.Config;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存缓存模块，单例模式，内部存储三种数据buffer和cacheRecords，以及recordToHBase
 * 从kafka读入的数据先存储在recordToHBase，再由持久化模块不断将recordToHBase中的数据存入HBase中，然后生成元数据，将它保存在buffer中
 * 当buffer数据量达到一定时，将buffer持久化，并加入cacheRecords，buffer清空
 */
public class MemoryCacheImpl<A1, A2, D> {
    private static final Logger logger = LoggerFactory.getLogger(MemoryCacheImpl.class);
    private static MemoryCacheImpl memoryCache;
    private Config conf;
    private int flushProgram = 0; //flush 方案 0 定期flush  1 定量flush
    private Integer bufferSizeMax = 1000; // buffer存储上限，默认1000
    private BatchBufferQueue<FaceObject> faceObjects; //这里应该是一个类似阻塞队列的集合
    private Map<Triplet<A1, A2, String>, List<Pair<String, D>>> cacheRecords;
    private DoubleBufferQueue<Quintuple<A1, A2, String, String, D>> buffer;


    private MemoryCacheImpl(Config conf){
        this.conf = conf;
        init(conf);
    }

    public static <A1, A2, D> MemoryCacheImpl<A1, A2, D> getInstance(Config conf){
        if(memoryCache == null){
            memoryCache = new MemoryCacheImpl<A1, A2, D>(conf);
        }
        return memoryCache;
    }

    public static <A1, A2, D> MemoryCacheImpl<A1, A2, D> getInstance(){
        if(memoryCache == null){
            Config config = Config.getConf();
            memoryCache = new MemoryCacheImpl<A1, A2, D>(config);
        }
        return memoryCache;
    }

    private void init(Config conf) {
        bufferSizeMax = conf.getValue(Config.WORKER_BUFFER_SIZE_MAX, bufferSizeMax);
        faceObjects = new BatchBufferQueue<>();
        cacheRecords = new ConcurrentHashMap<>();//ConcurrentHashMap
        buffer = new DoubleBufferQueue<>();
    }

    /**
     * 返回faceObjects
     * @return
     */
    public List<FaceObject> getObjects() {
        return faceObjects.get();
    }

    public List<Quintuple<A1, A2, String, String, D>> getBuffer(){
        return buffer.get();
    }

    /**
     * 返回cacheRecords
     * @return
     */
    public Map<Triplet<A1, A2, String>, List<Pair<String, D>>> getCacheRecords() {
        return cacheRecords;
    }

    public void setBufferSizeMax(int size) {
        this.bufferSizeMax = size;
    }

    /**
     * 增加recordToHBase
     */
    public void addFaceObjects(List<FaceObject> objs) {
        if(objs.size() > 0) {
            faceObjects.push(objs);
        }
    }

    /**
     * 增加多条record
     * @param records
     */
    public void loadCacheRecords(Map<Triplet<A1, A2, String>, List<Pair<String, D>>> records) {
        for(Map.Entry<Triplet<A1, A2, String>, List<Pair<String, D>>> entry : records.entrySet()){
            Triplet<A1, A2, String> key = entry.getKey();
            List<Pair<String, D>> value = entry.getValue();
            List<Pair<String, D>> list = cacheRecords.get(key);
            if(list == null || list.size() == 0){
                cacheRecords.put(key, value);
            }else {
                list.addAll(value);
            }
        }
    }

    /**
     * 将多条记录加入buffer，然后检查buffer是否满了
     * @param records 要添加的记录
     */
    public void addBuffer(List<Quintuple<A1, A2, String, String, D>> records) {
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
    private void check() {
        logger.info("To check The Buferr if it is to be flushed.");
        if(buffer.getWriteListSize() >= bufferSizeMax){
            TaskToHandleQueue.getTaskQueue().addTask(new FlushTask<>(buffer.getWithoutRemove()));
            moveToCacheRecords(buffer.get());
        }
    }

    public void flush(){
        if(buffer.getWriteListSize() > 0) {
            logger.info("To flush the buffer.");
            TaskToHandleQueue.getTaskQueue().addTask(new FlushTask<>(buffer.getWithoutRemove()));
            moveToCacheRecords(buffer.get());
        }
    }

    public void showMemory(){
        int cacheCount = 0;
        for(Map.Entry<Triplet<A1, A2, String>, List<Pair<String, D>>> entry : cacheRecords.entrySet()){
            cacheCount += entry.getValue().size();
        }
        logger.info("The size of cache is : " + cacheCount);
        logger.info("The size of faceObject is : " + faceObjects.size());
        logger.info("The size of buffer is : " + buffer.getWriteListSize());
    }

    /**
     * 将数据加入cacheRecords
     */
    public void moveToCacheRecords(List<Quintuple<A1, A2, String, String, D>> records) {
        logger.info("Move records from buffer to cacheRecords.");
        for(Quintuple<A1, A2, String, String, D> record : records){
            Triplet<A1, A2, String> key =
                    new Triplet<>(record.getFirst(), record.getSecond(), record.getThird());

            Pair<String, D> value = new Pair<>(record.getFourth(), record.getFifth());
            List<Pair<String, D>> list = cacheRecords.computeIfAbsent(key, k -> new CustomizeArrayList<>());
            list.add(value);
        }
    }
}
