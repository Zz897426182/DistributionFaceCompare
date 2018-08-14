package com.hzgc.compare.worker;

import com.hzgc.compare.rpc.client.result.AllReturn;
import com.hzgc.compare.rpc.server.annotation.RpcService;
import com.hzgc.compare.worker.common.CompareParam;
import com.hzgc.compare.worker.common.Feature;
import com.hzgc.compare.worker.common.SearchResult;
import com.hzgc.compare.worker.compare.ComparatorsImpl2;
import com.hzgc.compare.worker.compare.task.CompareNotSamePerson2;
import com.hzgc.compare.worker.compare.task.CompareOnePerson2;
import com.hzgc.compare.worker.compare.task.CompareSamePerson2;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.persistence.HBaseClient;
import com.hzgc.compare.worker.util.DateUtil;
import com.hzgc.compare.worker.util.FaceObjectUtil;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RpcService(Service.class)
public class ServiceImpl2 implements Service{
    private static final Logger logger = LoggerFactory.getLogger(ServiceImpl2.class);
    private Config conf;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private ExecutorService pool;
    private int daysPerThread = 2; //分割成的时间段
    private int daysToComapreMax = 6; //不使用多线程对比的最大时间段
    private int excutors = 12;

    public ServiceImpl2(){
        this.conf = Config.getConf();
        excutors = conf.getValue(Config.WORKER_EXECUTORS_TO_COMPARE, excutors);
        pool = Executors.newFixedThreadPool(excutors);
    }

    @Override
    public AllReturn<SearchResult> retrievalOnePerson(CompareParam param) {
        logger.info("The param is : " + FaceObjectUtil.objectToJson(param));
        String dateStart = param.getDateStart();
        String dateEnd = param.getDateEnd();
        long time1 = System.currentTimeMillis();
        try {
            if(sdf.parse(param.getDateEnd()).getTime() - sdf.parse(param.getDateStart()).getTime() >
                    1000L * 60 * 60 * 24 * daysToComapreMax){
                logger.info("The period of retrieval is large than predict.");
                logger.info("Splite the period.");
                List<String> periods = DateUtil.getPeriod(param.getDateStart(), param.getDateEnd(), daysPerThread);
                List<CompareOnePerson2> list = new ArrayList<>();
                for(String period : periods){
                    String[] time = period.split(",");
                    CompareOnePerson2 compareOnePerson2 = new CompareOnePerson2(param, time[0], time[1]);
                    pool.submit(compareOnePerson2);
                    list.add(compareOnePerson2);
                }

                while (true){
                    boolean flug = true;
                    for(CompareOnePerson2 compare : list){
                        flug = compare.isEnd() && flug;
                    }
                    if(flug){
                        break;
                    }
                }
                SearchResult result = new SearchResult();
                for(CompareOnePerson2 compare : list){
                    result.merge(compare.getSearchResult());
                }
                return new AllReturn<>(result);
            } else {
                CompareOnePerson2 compareOnePerson2 = new CompareOnePerson2(param, dateStart, dateEnd);
                SearchResult result = compareOnePerson2.compare();
                return new AllReturn<>(result);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return new AllReturn<>(null);
        }finally {
            long time2 = System.currentTimeMillis();
            logger.info("The time used of this Compare is : " + (time2 - time1));
        }

    }

    @Override
    public AllReturn<SearchResult> retrievalSamePerson(CompareParam param) {
        logger.info("The param is : " + FaceObjectUtil.objectToJson(param));
        String dateStart = param.getDateStart();
        String dateEnd = param.getDateEnd();
        long time1 = System.currentTimeMillis();
        try {
            if(sdf.parse(param.getDateEnd()).getTime() - sdf.parse(param.getDateStart()).getTime() >
                    1000L * 60 * 60 * 24 * daysToComapreMax){
                logger.info("The period of retrieval is large than predict.");
                logger.info("Splite the period.");
                List<String> periods = DateUtil.getPeriod(param.getDateStart(), param.getDateEnd(), daysPerThread);
                List<CompareSamePerson2> list = new ArrayList<>();
                for(String period : periods){
                    String[] time = period.split(",");
                    CompareSamePerson2 compareOnePerson2 = new CompareSamePerson2(param, time[0], time[1]);
                    pool.submit(compareOnePerson2);
                    list.add(compareOnePerson2);
                }

                while (true){
                    boolean flug = true;
                    for(CompareSamePerson2 compare : list){
                        flug = compare.isEnd() && flug;
                    }
                    if(flug){
                        break;
                    }
                }
                SearchResult result = new SearchResult();
                for(CompareSamePerson2 compare : list){
                    result.merge(compare.getSearchResult());
                }
                return new AllReturn<>(result);
            } else {
                CompareSamePerson2 compareOnePerson2 = new CompareSamePerson2(param, dateStart, dateEnd);
                SearchResult result = compareOnePerson2.compare();
                return new AllReturn<>(result);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return new AllReturn<>(null);
        }finally {
            long time2 = System.currentTimeMillis();
            logger.info("The time used of this Compare is : " + (time2 - time1));
        }
    }

    @Override
    public AllReturn<Map<String, SearchResult>> retrievalNotSamePerson(CompareParam param) {
        logger.info("The param is : " + FaceObjectUtil.objectToJson(param));
        String dateStart = param.getDateStart();
        String dateEnd = param.getDateEnd();
        long time1 = System.currentTimeMillis();
        try {
            if(sdf.parse(param.getDateEnd()).getTime() - sdf.parse(param.getDateStart()).getTime() >
                    1000L * 60 * 60 * 24 * daysToComapreMax){
                logger.info("The period of retrieval is large than predict.");
                logger.info("Splite the period.");
                List<String> periods = DateUtil.getPeriod(param.getDateStart(), param.getDateEnd(), daysPerThread);
                List<CompareNotSamePerson2> list = new ArrayList<>();
                for(String period : periods){
                    String[] time = period.split(",");
                    CompareNotSamePerson2 compareOnePerson2 = new CompareNotSamePerson2(param, time[0], time[1]);
                    pool.submit(compareOnePerson2);
                    list.add(compareOnePerson2);
                }

                while (true){
                    boolean flug = true;
                    for(CompareNotSamePerson2 compare : list){
                        flug = compare.isEnd() && flug;
                    }
                    if(flug){
                        break;
                    }
                }
                Map<String, SearchResult> result = new Hashtable<>();
                int index = 0;
                for(CompareNotSamePerson2 compare : list){
                    if(index == 0){
                        result = compare.getSearchResult();
                    } else{
                        for(String key : result.keySet()){
                            result.get(key).merge(compare.getSearchResult().get(key));
                        }
                    }
                    index ++;
                }
                return new AllReturn<>(result);
            } else {
                CompareNotSamePerson2 compareOnePerson2 = new CompareNotSamePerson2(param, dateStart, dateEnd);
                Map<String, SearchResult> result = compareOnePerson2.compare();
                return new AllReturn<>(result);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return new AllReturn<>(null);
        }finally {
            long time2 = System.currentTimeMillis();
            logger.info("The time used of this Compare is : " + (time2 - time1));
        }
    }

    public AllReturn<String> test() throws InterruptedException {
        Thread.sleep(1000L * 10);
        logger.info("TEST ");
        return new AllReturn<>("response");
    }
}
