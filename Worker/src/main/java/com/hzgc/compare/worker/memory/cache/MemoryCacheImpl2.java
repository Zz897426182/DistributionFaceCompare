package com.hzgc.compare.worker.memory.cache;

import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.Quintuple;
import com.hzgc.compare.worker.conf.Config;
import java.util.List;

/**
 * 内存缓存模块，单例模式，内部存储三种数据buffer和cacheRecords，以及recordToHBase
 * 从kafka读入的数据先存储在recordToHBase，再由持久化模块不断将recordToHBase中的数据存入HBase中，然后生成元数据，将它保存在buffer中
 * 当buffer数据量达到一定时，将buffer持久化，并加入cacheRecords，buffer清空
 */
public class MemoryCacheImpl2 implements MemoryCache<List<Quintuple<String, String, String, String, byte[]>>>{
    private static MemoryCacheImpl2 memoryCache;
    private Config conf;
    private Integer bufferSizeMax = 500; // buffer存储上限，默认500
    private List<FaceObject> recordToHBase;
    private List<Quintuple<String, String, String, String, byte[]>> cacheRecords;
    private List<Quintuple<String, String, String, String, byte[]>> buffer;


    private MemoryCacheImpl2(Config conf){
        this.conf = conf;
        init(conf);
    }

    public static MemoryCacheImpl2 getInstance(Config conf){
        if(memoryCache == null){
            memoryCache = new MemoryCacheImpl2(conf);
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
    public List<Quintuple<String, String, String, String, byte[]>> getCacheRecords() {
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
    public void recordToHBase(FaceObject obj) {

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
    public void loadCacheRecords(List<Quintuple<String, String, String, String, byte[]>> records) {

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
