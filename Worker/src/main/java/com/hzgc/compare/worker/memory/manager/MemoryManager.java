package com.hzgc.compare.worker.memory.manager;

import com.hzgc.compare.worker.conf.Config;

public class MemoryManager {
    private Config conf;
    private Long cacheNumMax = 30000000L; //内存中存储数据的上限，默认值3000万，根据实际内存设置
    private Long checkTime = 1000L * 60 * 30; //内存检查时间间隔， 默认30分钟
    private Integer recordTimeOut = 360; //一级过期时间，单位： 天 ， 默认12个月，为了一次就删除到0.8以下，需要根据实际情况设置好这个值

    public MemoryManager(Config conf){
        this.conf = conf;
        init(conf);
    }
    /**
     * 根据conf中的参数，来设置MemeryManager需要的参数
     * @param conf
     */
    void init(Config conf){
        cacheNumMax = conf.getValue(Config.WORKER_CACHE_SIZE_MAX, cacheNumMax);
        checkTime = conf.getValue(Config.WORKER_MEMORY_CHECK_TIME, checkTime);
        recordTimeOut = conf.getValue(Config.WORKER_RECORD_TIME_OUT, recordTimeOut);
    }

    /**
     * 启动定期任务，检查内存数据是否达到上限，如果是，调用remove
     * @return
     */
    public void startToCheck(){

    }

    /**
     * 遍历内存中的缓存，删除时间超过一级过期时间的数据，并保存下当前有效时间的最小值
     * 然后检查数据，如果还是不符合要求，删除超过二级过期时间的数据，
     * 二级过期时间设置为一级过期时间减十天，以次类推
     * 直到数据量减少到阈值的80%以下
     */
    public void remove(){

    }

    /**
     * 判断该时间是否在内存中（与当前有效时间的最小值对比）
     * @param time
     * @return
     */
    public boolean isOutOfTime(String time){
        return false;
    }
}
