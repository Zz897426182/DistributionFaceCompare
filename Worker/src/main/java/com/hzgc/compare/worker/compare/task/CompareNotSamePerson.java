package com.hzgc.compare.worker.compare.task;

import com.hzgc.compare.worker.common.CompareParam;
import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.Feature;
import com.hzgc.compare.worker.common.SearchResult;
import com.hzgc.compare.worker.compare.Comparators;
import com.hzgc.compare.worker.compare.ComparatorsImpl;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.persistence.HBaseClient;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompareNotSamePerson implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CompareNotSamePerson.class);
    private int resultDefaultCount = 20;
    private Config conf;
    private CompareParam param;
    private String dateStart;
    private String dateEnd;
    boolean isEnd = false;
    private int compareSize = 500;
    Map<String, SearchResult> searchResult;

    public Map<String, SearchResult> getSearchResult(){
        return searchResult;
    }

    public boolean isEnd(){
        return isEnd;
    }

    public CompareNotSamePerson(CompareParam param, String dateStart, String dateEnd){
        this.param = param;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }


    public Map<String, SearchResult> compare() {
        Map<String, SearchResult> result = new HashMap<>();
        List<String> ipcIdList = param.getArg1List();
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
            return result;
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
            return result;
        }
    }

    @Override
    public void run() {
        searchResult = compare();
        isEnd = true;
    }
}
