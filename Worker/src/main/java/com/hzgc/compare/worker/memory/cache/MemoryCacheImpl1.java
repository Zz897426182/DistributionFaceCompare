package com.hzgc.compare.worker.memory.cache;

import com.hzgc.compare.worker.common.CustomizeBlockingQueue;
import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.Quintuple;
import com.hzgc.compare.worker.common.Triplet;
import com.hzgc.compare.worker.conf.Config;
import javafx.util.Pair;

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
    private Integer bufferSizeMax = 500; // buffer存储上限，默认500
    private CustomizeBlockingQueue<FaceObject> recordToHBase; //这里应该是一个类似阻塞队列的集合
    private Map<Triplet<String, String, String>, List<Pair<String, byte[]>>> cacheRecords;
    private List<Quintuple<String, String, String, String, byte[]>> buffer;


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

    private void init(Config conf) {

    }

    /**
     * 返回recordToHBase
     * @return
     */
    public List<FaceObject> getObjects() {
        return null;
    }

    /**
     * 返回cacheRecords
     * @return
     */
    public Map<Triplet<String, String, String>, List<Pair<String, byte[]>>> getCacheRecords() {
        return null;
    }

    /**
     * 返回buffer
     * @return
     */
    public List<Quintuple<String, String, String, String, byte[]>> getBuffer() {
        return null;
    }

    /**
     * 增加recordToHBase
     */
    public void recordToHBase(List<FaceObject> objs) {

    }

    /**
     * 将buffer加入到CacheRecords
     */
    public void addCacheRecords() {

    }

    /**
     * 增加多条record
     * @param records
     */
    public void loadCacheRecords(Map<Triplet<String, String, String>, List<Pair<String, byte[]>>> records) {

    }

    /**
     * 将多条记录加入buffer，然后检查buffer是否满了
     * @param records
     */
    public void addBuffer(List<Quintuple<String, String, String, String, byte[]>> records) {

    }

    /**
     * 检查buffer是否满了, 如果满了，则在TaskToHandle中添加一个FlushTask任务,并将buffer加入cacheRecords，buffer重新创建
     */
    public void check() {

    }

    /**
     * 将buffer中的数据加入cacheRecords
     */
    public void moveBufferToCacheRecords() {

    }
}
