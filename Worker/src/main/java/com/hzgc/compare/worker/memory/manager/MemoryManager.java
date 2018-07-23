package com.hzgc.compare.worker.memory.manager;

import com.hzgc.compare.worker.common.Triplet;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MemoryManager<A1,A2,D> {
    private static final Logger logger = LoggerFactory.getLogger(MemoryManager.class);
    private Config conf;
    private Long cacheNumMax = 30000000L; //内存中存储数据的上限，默认值3000万，根据实际内存设置
    private Long checkTime = 1000L * 60 * 30; //内存检查时间间隔， 默认30分钟
    private long recordTimeOut = 360; //一级过期时间，单位： 天 ， 默认12个月，为了一次就删除到0.8以下，需要根据实际情况设置好这个值
    private SimpleDateFormat sdf;
    public MemoryManager(){
        init();
    }
    /**
     * 根据conf中的参数，来设置MemeryManager需要的参数
     */
    void init(){
        conf = Config.getConf();
        cacheNumMax = conf.getValue(Config.WORKER_CACHE_SIZE_MAX, cacheNumMax);
        checkTime = conf.getValue(Config.WORKER_MEMORY_CHECK_TIME, checkTime);
        recordTimeOut = conf.getValue(Config.WORKER_RECORD_TIME_OUT, recordTimeOut);
        sdf = new SimpleDateFormat("yyyy-MM-dd");
    }

    public void reLoadParam(){
        init();
    }

    /**
     * 启动定期任务，检查内存数据是否达到上限，如果是，调用remove
     * @return
     */
    public void startToCheck(){
        logger.info("Start to check memory.");
        new Timer().schedule(new TimeToCheckMemory(this), checkTime, checkTime);
    }

    /**
     * 遍历内存中的缓存，删除时间超过一级过期时间的数据，并保存下当前有效时间的最小值
     * 然后检查数据，如果还是不符合要求，删除超过二级过期时间的数据，
     * 二级过期时间设置为一级过期时间减十天，以次类推
     * 直到数据量减少到阈值的80%以下
     */
    public void remove() {
        logger.info("To remove records time out.");
        removeTimeOut(recordTimeOut);
    }

    private void removeTimeOut(long timeOut){
        long count = 0L;
        MemoryCacheImpl cache = MemoryCacheImpl.getInstance(conf);
        Map<Triplet<A1, A2, String>, List<Pair<String, D>>> records = cache.getCacheRecords();
        List<Triplet<A1, A2, String>> keyList = new ArrayList<>();
        keyList.addAll(records.keySet());
        for(Triplet<A1, A2, String> key : keyList){
            String date = key.getThird();
            try {
                long time = sdf.parse(date).getTime();
                if(System.currentTimeMillis() - time > timeOut * 24L * 60 * 60 * 1000){
                    records.remove(key);
                } else {
                    count += records.get(key).size();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        System.out.println("The Num of Memory Cache is : " + count);
        if(count > cacheNumMax * 0.8){
            removeTimeOut(timeOut - 1);
        }
    }

    public void timeToCheckFlush(){
        logger.info("Start to flush buffer");
        new Timer().schedule(new TimeToFlushBuffer(), 5000, 5000);
    }

    /**
     * 判断该时间是否在内存中（与当前有效时间的最小值对比）
     * @param time
     * @return
     */
    public boolean isOutOfTime(String time){
        String oldest = "";
        MemoryCacheImpl cache = MemoryCacheImpl.getInstance(conf);
        Map<Triplet<A1, A2, String>, List<Pair<String, D>>> records = cache.getCacheRecords();
        Set<Triplet<A1, A2, String>> keySet = new HashSet<>();
        keySet.addAll(records.keySet());
        for(Triplet<A1, A2, String> key : keySet){
            String date = key.getThird();
            if(oldest.compareTo(date) < 0){
                oldest = date;
            }
        }
        return time.compareTo(oldest) >= 0;
    }

    public void setRecordTimeOut(int days){
        this.recordTimeOut = days;
    }
}
