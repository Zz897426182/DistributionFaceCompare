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
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl1;
import com.hzgc.compare.worker.persistence.HBaseClient;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RpcService(Service.class)
public class ServiceImpl implements Service{
    private int resultDefaultCount = 10;
    private int compareSize = 500000;
    private Config conf;

    public ServiceImpl(){
        this.conf = Config.getConf();
        resultDefaultCount = conf.getValue("", resultDefaultCount);
        compareSize = conf.getValue("", compareSize);
    }
//    public AllReturn<SearchResult> retrieval(List<String> arg1List, String arg2RangStart,
//                                             String arg2RangEnd, byte[] feature1, float[] feature2, int resultCount){
//        return new AllReturn<>(null);
//    }

    @Override
    public AllReturn<SearchResult> retrievalOnePerson(CompareParam param) {
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
        List<Pair<String, byte[]>> dataFilterd =  comparators.filter(ipcIdList, null, dateStart, dateEnd);
        if(dataFilterd.size() > compareSize){
            // 若过滤结果太大，则需要第一次对比
            List<String> firstCompared =  comparators.compareFirst(feature1, 500, dataFilterd);
            //根据对比结果从HBase读取数据
            List<FaceObject> objs =  client.readFromHBase(firstCompared);
            // 第二次对比
            result = comparators.compareSecond(feature2, sim, objs);
            //结果排序
            result.sortBySim();
            //取相似度最高的几个
            result = result.take(resultDefaultCount);
        }else {
            //若过滤结果比较小，则直接进行第二次对比
            List<FaceObject> objs = client.readFromHBase2(dataFilterd);
//            System.out.println("过滤结果" + objs.size() + " , " + objs.get(0));
            result = comparators.compareSecond(feature2, sim, objs);
            //结果排序
            result.sortBySim();
            //取相似度最高的几个
            result = result.take(resultCount);
        }
//        System.out.println("对比结果2" + result.getRecords().length + " , " + result.getRecords()[0]);
        return new AllReturn<>(result);
    }

    @Override
    public AllReturn<SearchResult> retrievalSamePerson(CompareParam param) {
        return null;
    }

    @Override
    public AllReturn<List<SearchResult>> retrievalNotSamePerson(CompareParam param) {
        List<SearchResult> result = new ArrayList<>();
        List<String> ipcIdList = param.getArg1List();
        String dateStart = param.getDateStart();
        String dateEnd = param.getDateEnd();
        List<Feature> features = param.getFeatures();
        float sim = param.getSim();
        HBaseClient client = new HBaseClient();
        // 根据条件过滤
        Comparators comparators = new ComparatorsImpl();
        List<Pair<String, byte[]>> dataFilterd =  comparators.filter(ipcIdList, null, dateStart, dateEnd);
        if(dataFilterd.size() > compareSize){
            // 若过滤结果太大，则需要第一次对比
            Map<Feature, List<String>> featureToRowkey = new HashMap<>();
            for(Feature feature : features){
                List<String> rowkeys = comparators.compareFirst(feature.getFeature1(), 500, dataFilterd);
                featureToRowkey.put(feature, rowkeys);
            }
            //根据对比结果从HBase读取数据
            Map<Feature, List<FaceObject>> objsMap = client.readFromHBase(featureToRowkey);
            for(Map.Entry<Feature, List<FaceObject>> entry : objsMap.entrySet()){
                SearchResult searchResult = comparators.compareSecond(entry.getKey().getFeature2(), sim, entry.getValue());
                //结果排序
                searchResult.sortBySim();
                //取相似度最高的几个
                searchResult = searchResult.take(resultDefaultCount);
                result.add(searchResult);
            }
            return new AllReturn<>(result);
        }
        //若过滤结果比较小，则直接进行第二次对比
        List<FaceObject> objs = client.readFromHBase2(dataFilterd);
        for(Feature feature : features){
            SearchResult searchResult = comparators.compareSecond(feature.getFeature2(), sim, objs);
            //结果排序
            searchResult.sortBySim();
            //取相似度最高的几个
            searchResult = searchResult.take(resultDefaultCount);
            result.add(searchResult);
        }
        return new AllReturn<>(result);
    }

    @Override
    public void stopTheWorker() {
        MemoryCacheImpl1 memoryCache = MemoryCacheImpl1.getInstance();
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
