package com.hzgc.compare.worker.memory.cache;

import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.Quintuple;

import java.util.List;

public interface MemoryCache<T> {

    /**
     * 返回recordToHBase
     * @return
     */
    public List<FaceObject> getObjects();

    /**
     * 返回cacheRecords
     * @return
     */
    public T getCacheRecords();

    /**
     * 返回buffer
     * @return
     */
    public List<Quintuple<String, String, String, String, byte[]>> getBuffer();

    /**
     * 增加recordToHBase
     */
    public void recordToHBase(List<FaceObject> objs);

    /**
     * 将buffer加入到CacheRecords
     */
    public void addCacheRecords();

    /**
     * 增加多条record
     */
    public void loadCacheRecords(T records);

    /**
     * 将多条记录加入buffer，然后检查buffer是否满了
     * @param records
     */
    public void addBuffer(List<Quintuple<String, String, String, String, byte[]>> records);

    /**
     * 检查buffer是否满了, 如果满了，则在TaskToHandle中添加一个FlushTask任务,并将buffer加入cacheRecords，buffer重新创建
     */
    public void check();
    /**
     * 将buffer中的数据加入cacheRecords
     */
    public void moveBufferToCacheRecords();

}
