package com.hzgc.compare.worker;

import com.hzgc.compare.worker.common.CompareParam;
import com.hzgc.compare.worker.common.Feature;
import com.hzgc.compare.rpc.client.result.AllReturn;
import com.hzgc.compare.rpc.server.annotation.RpcService;
import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.Quintuple;
import com.hzgc.compare.worker.common.SearchResult;
import com.hzgc.compare.worker.common.taskhandle.FlushTask;
import com.hzgc.compare.worker.common.taskhandle.TaskToHandleQueue;
import com.hzgc.compare.worker.compare.Comparators;
import com.hzgc.compare.worker.compare.ComparatorsImpl;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import com.hzgc.compare.worker.persistence.HBaseClient;
import com.hzgc.compare.worker.util.FaceObjectUtil;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class ServiceImpl implements Service{
    private static final Logger logger = LoggerFactory.getLogger(ServiceImpl.class);
    private int resultDefaultCount = 10;
    private int compareSize = 500;
    private Config conf;

    public ServiceImpl(){
        this.conf = Config.getConf();
        resultDefaultCount = conf.getValue("", resultDefaultCount);
        compareSize = conf.getValue("", compareSize);
    }

    @Override
    public AllReturn<SearchResult> retrievalOnePerson(CompareParam param) {
        logger.info("The param is : " + FaceObjectUtil.objectToJson(param));
        List<String> ipcIdList = param.getArg1List();
        String dateStart = param.getDateStart();
        String dateEnd = param.getDateEnd();
        byte[] feature1 = param.getFeatures().get(0).getFeature1();
        float[] feature2 = param.getFeatures().get(0).getFeature2();
        float sim = param.getSim();
        int resultCount = param.getResultCount();
        if (resultCount == 0){
            resultCount = resultDefaultCount;
        }
        SearchResult result;
        HBaseClient client = new HBaseClient();
        Comparators comparators = new ComparatorsImpl();
        // 根据条件过滤
        logger.info("To filter the records from memory.");
        List<Pair<String, byte[]>> dataFilterd =  comparators.<byte[]>filter(ipcIdList, null, dateStart, dateEnd);
        if(dataFilterd.size() > compareSize){
            // 若过滤结果太大，则需要第一次对比
            logger.info("The result of filter is too bigger , to compare it first.");
            List<String> firstCompared =  comparators.compareFirst(feature1, 500, dataFilterd);
            //根据对比结果从HBase读取数据
            logger.info("Read records from HBase with result of first compared.");
            List<FaceObject> objs =  client.readFromHBase(firstCompared);
            // 第二次对比
            logger.info("Compare records second.");
            result = comparators.compareSecond(feature2, sim, objs);
            //取相似度最高的几个
            logger.info("Take the top " + resultCount);
            result = result.take(resultCount);
        }else {
            //若过滤结果比较小，则直接进行第二次对比
            logger.info("Read records from HBase with result of filter.");
            List<FaceObject> objs = client.readFromHBase2(dataFilterd);
//            System.out.println("过滤结果" + objs.size() + " , " + objs.get(0));
            logger.info("Compare records second directly.");
            result = comparators.compareSecond(feature2, sim, objs);
            //取相似度最高的几个
            logger.info("Take the top " + resultCount);
            result = result.take(resultCount);
        }
//        System.out.println("对比结果2" + result.getRecords().length + " , " + result.getRecords()[0]);
        return new AllReturn<>(result);
    }

    @Override
    public AllReturn<SearchResult> retrievalSamePerson(CompareParam param) {
        logger.info("The param is : " + FaceObjectUtil.objectToJson(param));
        List<String> ipcIdList = param.getArg1List();
        String dateStart = param.getDateStart();
        String dateEnd = param.getDateEnd();
        List<Feature> features = param.getFeatures();
        float sim = param.getSim();
        int resultCount = param.getResultCount();
        if (resultCount == 0){
            resultCount = resultDefaultCount;
        }
        List<byte[]> feature1List = new ArrayList<>();
        List<float[]> feature2List = new ArrayList<>();
        for (Feature feature : features) {
            feature1List.add(feature.getFeature1());
            feature2List.add(feature.getFeature2());
        }
        SearchResult result;
        HBaseClient client = new HBaseClient();
        Comparators comparators = new ComparatorsImpl();
        // 根据条件过滤
        logger.info("To filter the records from memory.");
        List<Pair<String, byte[]>> dataFilterd =  comparators.<byte[]>filter(ipcIdList, null, dateStart, dateEnd);
        if(dataFilterd.size() > compareSize) {
            // 若过滤结果太大，则需要第一次对比
            logger.info("The result of filter is too bigger , to compare it first.");
            List<String> firstCompared = comparators.compareFirstTheSamePerson(feature1List, 500, dataFilterd);
            //根据对比结果从HBase读取数据
            logger.info("Read records from HBase with result of first compared.");
            List<FaceObject> objs =  client.readFromHBase(firstCompared);
            // 第二次对比
            logger.info("Compare records second.");
            result = comparators.compareSecondTheSamePerson(feature2List, sim, objs);
            //取相似度最高的几个
            logger.info("Take the top " + resultCount);
            result = result.take(resultCount);
        } else {
            //若过滤结果比较小，则直接进行第二次对比
            logger.info("Read records from HBase with result of filter.");
            List<FaceObject> objs = client.readFromHBase2(dataFilterd);
            logger.info("Compare records second directly.");
            result = comparators.compareSecondTheSamePerson(feature2List, sim, objs);
            //取相似度最高的几个
            logger.info("Take the top " + resultCount);
            result = result.take(resultCount);
        }
        return new AllReturn<>(result);
    }

    @Override
    public AllReturn<Map<String, SearchResult>> retrievalNotSamePerson(CompareParam param) {
        logger.info("The param is : " + FaceObjectUtil.objectToJson(param));
        Map<String, SearchResult> result = new HashMap<>();
        List<String> ipcIdList = param.getArg1List();
        String dateStart = param.getDateStart();
        String dateEnd = param.getDateEnd();
        List<Feature> features = param.getFeatures();
        float sim = param.getSim();
        int resultCount = param.getResultCount();
        if (resultCount <= 0 || resultCount > 50){
            resultCount = resultDefaultCount;
        }
        HBaseClient client = new HBaseClient();
        // 根据条件过滤
        Comparators comparators = new ComparatorsImpl();
        logger.info("To filter the records from memory.");
        List<Pair<String, byte[]>> dataFilterd =  comparators.filter(ipcIdList, null, dateStart, dateEnd);
        if(dataFilterd.size() > compareSize){
            // 若过滤结果太大，则需要第一次对比
            logger.info("The result of filter is too bigger , to compare it first.");
            List<String> Rowkeys = comparators.compareFirstNotSamePerson(features, 500, dataFilterd);
            //根据对比结果从HBase读取数据
            logger.info("Read records from HBase with result of first compared.");
            List<FaceObject> objs = client.readFromHBase(Rowkeys);
            logger.info("Compare records second.");
            Map<String, SearchResult> resultTemp = comparators.compareSecondNotSamePerson(features, sim, objs);
            logger.info("Take the top " + resultCount);
            for(Map.Entry<String, SearchResult> searchResult : resultTemp.entrySet()){
                //取相似度最高的几个
                SearchResult searchResult1 = searchResult.getValue().take(resultCount);
                result.put(searchResult.getKey(), searchResult1);
            }
            return new AllReturn<>(result);
        } else {
            //若过滤结果比较小，则直接进行第二次对比
            logger.info("Read records from HBase with result of filter.");
            List<FaceObject> objs = client.readFromHBase2(dataFilterd);
            logger.info("Compare records second directly.");
            Map<String, SearchResult> resultTemp = comparators.compareSecondNotSamePerson(features, sim, objs);
            logger.info("Take the top " + resultCount);
            for(Map.Entry<String, SearchResult> searchResult : resultTemp.entrySet()){
                //取相似度最高的几个
                SearchResult searchResult1 = searchResult.getValue().take(resultCount);
                result.put(searchResult.getKey(), searchResult1);
            }
            return new AllReturn<>(result);
        }
    }

    @Override
    public void stopTheWorker() {
        MemoryCacheImpl memoryCache = MemoryCacheImpl.getInstance();
        List<Quintuple<String, String, String, String, byte[]>> buffer = memoryCache.getBuffer();
        memoryCache.moveToCacheRecords(buffer);
        TaskToHandleQueue.getTaskQueue().addTask(new FlushTask(buffer));
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
